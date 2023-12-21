import * as moment from 'moment';

import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  UntypedFormArray,
  UntypedFormControl,
  UntypedFormGroup,
  Validators,
} from '@angular/forms';
import { AuctionDayAheadType, AuctionOfferStatus, AuctionStatus, AuctionType } from '../enums';
import { AuctionOfferDTO, AuctionOfferDerDTO } from '../offers/offer';
import { BidDerFormType, BidFormType } from './models';
import { ContentType, Role } from '@app/shared/enums';
import { Dictionary, FileDTO, FlexPotentialMinimalDTO } from '@app/shared/models';
import { DownloadService, HttpService } from '@app/core';
import { Observable, map, of } from 'rxjs';

import { AuctionBidBandService } from './bid-band.service';
import { AuctionCmvcDTO } from '../cm-vc/cm-vc';
import { AuctionDayAheadDTO } from '../day-ahead/day-ahead';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MaxOrSameValidator } from '@app/shared/commons/validators';
import { AllSelfScheduleVolumesDTO, SelfScheduleVolumesDTO } from '@app/content/units/tabs/self-schedules/self-schedule';
import { TranslateService } from '@ngx-translate/core';
import { isNil } from 'lodash-es';
import { CUSTOM_HANDLE_ERROR_CONTEXT } from '@app/core/auth/auth.interceptor';

export type AuctionOfferTypeDTO = AuctionCmvcDTO | AuctionDayAheadDTO | undefined;

@Injectable()
export class AuctionBidService extends HttpService {
  auctionType: AuctionType = AuctionType.CMVC;
  private lowestAllowedVolume = 0;
  private maximumAllowedVolume = 0;

  get url(): string {
    switch (this.auctionType) {
      case AuctionType.CMVC:
        return 'api/user/auctions-cmvc/offers';
      default:
        return 'api/user/auctions-day-ahead/offers';
    }
  }

  constructor(
    httpClient: HttpClient,
    private fb: FormBuilder,
    private translate: TranslateService,
    private bandService: AuctionBidBandService
  ) {
    super(httpClient);
  }

  controlMethodInsideFormArray(formArray: UntypedFormArray | undefined, key: string, execute: (control: UntypedFormControl) => void): void {
    if (!formArray) {
      return;
    }

    formArray.controls.forEach((groupControl: AbstractControl) => {
      const control = groupControl.get(key) as UntypedFormControl;

      if (control) {
        execute(control);
      }
    });
  }

  createDersGroup(
    hasDers: boolean,
    auctionType: AuctionType,
    bid: Partial<AuctionOfferDTO> | undefined,
    data: Partial<AuctionOfferDTO> = {},
    derData: Partial<AuctionOfferDerDTO> = {},
    auction: Partial<AuctionCmvcDTO | AuctionDayAheadDTO> = {},
    hours: string[],
    imported?: boolean
  ): FormGroup<BidDerFormType> {
    const bidIsPending =
      bid === undefined || bid?.status === AuctionOfferStatus.PENDING || bid?.status === AuctionOfferStatus.VOLUMES_VERIFIED;
    const type: AuctionDayAheadType = this.getAuctionOfferType(auctionType, bid, auction);
    const isDayAhead = auctionType === AuctionType.DAY_AHEAD;
    const isEdit = !!data.id;

    const auctionIsOpen = this.getStatusForAuctionOfferType(type, 'open').includes(auction.status || AuctionStatus.NEW);
    const isCapacityDayAhead = isDayAhead && (auction as AuctionDayAheadDTO).type === AuctionDayAheadType.CAPACITY;
    const volumeValidators = this.getVolumeRangeValidation(auctionType, data, auction);
    const commonPrice = isCapacityDayAhead ? data.commonPrice : null;
    const auctionIsClosed = !auctionIsOpen;

    const form = this.fb.group<BidDerFormType>({
      der: this.fb.control(
        {
          value: derData.der,
          disabled:
            auctionType !== AuctionType.DAY_AHEAD || imported
              ? false
              : (!data.schedulingUnit?.bsp && !hasDers) || !auctionIsOpen || !bidIsPending,
        },
        Validators.required
      ),
      derDisabled: this.fb.control({ value: derData.der ?? null, disabled: true }),
      price: this.fb.control(
        { value: derData.price, disabled: imported ? false : !auctionIsOpen },
        !isDayAhead ? Validators.required : null
      ),
      volume: this.fb.control(
        { value: derData.volume, disabled: imported ? false : !auctionIsOpen },
        !isDayAhead ? [Validators.required, ...volumeValidators] : [...volumeValidators]
      ),
      acceptedVolume: this.fb.control(
        { value: derData.acceptedVolume, disabled: !data.volumeDivisibility || !auctionIsClosed || !bidIsPending },
        isEdit && !isDayAhead ? [Validators.required, MaxOrSameValidator(derData.volume)] : null
      ),
    });

    if (isDayAhead) {
      const bandData = this.bandService.addMissingBand(
        bid,
        hours,
        auctionIsClosed,
        derData.bandData ?? [],
        auction as AuctionDayAheadDTO,
        commonPrice
      );
      const limitExceedValues = {
        der: derData?.der,
        selfSchedule: derData?.bandData?.filter(item => item.bandNumber === 0),
      };
      const bandDataGroups = this.bandService
        .mapBandData(bandData, hours, auction as AuctionDayAheadDTO)
        .map(({ bandNumber, data: bandData }) =>
          this.bandService.createDerBandDataGroup(
            bandNumber,
            bandData,
            data,
            auctionIsOpen ? 'OPEN' : 'CLOSED',
            type,
            false,
            isEdit,
            limitExceedValues
          )
        );

      form.addControl('bandData', this.fb.array(bandDataGroups));
    }

    return form;
  }

  createForm<T>(
    data: Partial<AuctionOfferDTO> = {},
    auction: Partial<AuctionCmvcDTO | AuctionDayAheadDTO> = {},
    bid: Partial<AuctionOfferDTO> | undefined,
    auctionType: AuctionType,
    fromBids: boolean,
    hours: string[],
    imported?: boolean
  ): FormGroup<BidFormType> {
    const volumeValidators = this.getVolumeRangeValidation(auctionType, data, auction);
    const type: AuctionDayAheadType = this.getAuctionOfferType(auctionType, bid, auction);
    const isEdit = !!data.id;

    if (auctionType === AuctionType.DAY_AHEAD) {
      const auctionDa = auction as Partial<AuctionDayAheadDTO>;

      const minDesiredPower = type === AuctionDayAheadType.CAPACITY ? auctionDa?.minDesiredCapacity : auctionDa?.minDesiredEnergy;
      const maxDesiredPower = type === AuctionDayAheadType.CAPACITY ? auctionDa?.maxDesiredCapacity : auctionDa?.maxDesiredEnergy;

      auctionDa.minDesiredPower = minDesiredPower;
      auctionDa.maxDesiredPower = maxDesiredPower;
    }

    let auctionIsOpen = this.getStatusForAuctionOfferType(type, 'open').includes(auction.status || AuctionStatus.NEW);
    const { period, periodFrom, periodTo } = this.getDefaultDeliveryPeriod(auction, bid, auctionType);
    const deliveryPeriod = periodFrom ? moment(periodFrom).startOf('day').toDate() : period;
    const dersData: AuctionOfferDerDTO[] = data.ders && data.ders.length ? data.ders : [{}];

    if (bid && bid.status !== AuctionOfferStatus.PENDING) {
      auctionIsOpen = false;
    }

    const isDayAhead = auctionType === AuctionType.DAY_AHEAD;
    const isCapacityDayAhead = isDayAhead && (auction as AuctionDayAheadDTO).type === AuctionDayAheadType.CAPACITY;
    const commonPrice = isCapacityDayAhead ? data.commonPrice ?? null : null;

    const form = this.fb.group<BidFormType>({
      id: this.fb.control({ value: data.id ?? null, disabled: true }),
      auctionId: this.fb.control({ value: data.auctionId ?? null, disabled: true }),

      schedulingUnit: this.fb.control(
        { value: data.schedulingUnit, disabled: auctionType === AuctionType.CMVC || !auctionIsOpen || fromBids },
        Validators.required
      ),
      flexPotential: this.fb.control(
        { value: data.flexPotential, disabled: auctionType === AuctionType.DAY_AHEAD || !auctionIsOpen || fromBids },
        Validators.required
      ),
      ders: this.fb.array(
        dersData.map((derData: AuctionOfferDerDTO) =>
          this.createDersGroup(false, auctionType, bid, data, derData, auction, hours, imported)
        )
      ),
      price: this.fb.control({ value: data.price ?? null, disabled: !auctionIsOpen }, Validators.required),
      volume: this.fb.control({ value: data.volume ?? null, disabled: !auctionIsOpen }, [Validators.required, ...volumeValidators]),
      volumeDivisibility: this.fb.control({
        value: isEdit ? !!data.volumeDivisibility : true,
        disabled: imported ? false : !auctionIsOpen,
      }),

      commonPriceOfBid: this.fb.control({ value: isDayAhead ? !!commonPrice : false, disabled: imported ? false : !auctionIsOpen }),
      commonPrice: this.fb.control({ value: isDayAhead ? commonPrice : null, disabled: imported ? false : !auctionIsOpen }),
      bandNumber: this.fb.control({ value: null, disabled: imported ? false : !auctionIsOpen }),
      hourNumber: this.fb.control({ value: null, disabled: imported ? false : !auctionIsOpen }),

      deliveryPeriodDivisibility: this.fb.control({
        value: isEdit ? !!data.deliveryPeriodDivisibility : true,
        disabled: imported ? false : !auctionIsOpen,
      }),
      deliveryPeriod: this.fb.nonNullable.control({ value: deliveryPeriod, disabled: !imported }, Validators.required),
      deliveryPeriodFrom: this.fb.control(
        { value: this.formatToDate(data.deliveryPeriodFrom, periodFrom), disabled: imported ? false : !auctionIsOpen },
        Validators.required
      ),
      deliveryPeriodTo: this.fb.control(
        { value: this.formatToDate(data.deliveryPeriodTo, periodTo), disabled: imported ? false : !auctionIsOpen },
        Validators.required
      ),

      acceptedVolume: this.fb.control({ value: data.acceptedVolume ?? null, disabled: true }),
      acceptedDeliveryPeriod: this.fb.control({
        value: this.formatToDate(data.acceptedDeliveryPeriodFrom, periodFrom),
        disabled: true,
      }),
      acceptedDeliveryPeriodFrom: this.fb.control({
        value: this.formatToDate(data.acceptedDeliveryPeriodFrom, periodFrom),
        disabled: true,
      }),
      acceptedDeliveryPeriodTo: this.fb.control({
        value: this.formatToDate(data.acceptedDeliveryPeriodTo, periodTo),
        disabled: true,
      }),
    });

    if (auctionType === AuctionType.DAY_AHEAD) {
      form.removeControl('acceptedVolume');
      form.removeControl('volume');
      form.removeControl('price');
    } else {
      form.removeControl('ders');
    }

    if (this.maximumAllowedVolume < this.lowestAllowedVolume) {
      if (auctionType === AuctionType.DAY_AHEAD) {
        this.controlMethodInsideFormArray(form.controls.ders, 'volume', (control: UntypedFormControl) => {
          control?.markAllAsTouched();
          control?.updateValueAndValidity();
        });
      } else {
        form.controls.volume?.markAsTouched();
        form.controls.volume?.updateValueAndValidity();
      }
    }

    return form;
  }

  getAuctionStatus(
    auctionType: AuctionType,
    bid: Partial<AuctionOfferDTO> | undefined,
    auction: Partial<AuctionCmvcDTO | AuctionDayAheadDTO> = {}
  ): 'OPEN' | 'CLOSED' {
    const type: AuctionDayAheadType = this.getAuctionOfferType(auctionType, bid, auction);
    const auctionIsOpen = this.getStatusForAuctionOfferType(type, 'open').includes(auction.status || AuctionStatus.NEW);

    return auctionIsOpen ? 'OPEN' : 'CLOSED';
  }

  getAllowedVolumeRange() {
    return { min: this.lowestAllowedVolume, max: this.maximumAllowedVolume };
  }

  getBsps(productId?: number): Observable<Dictionary<number>[]> {
    if (!productId) {
      return of([]);
    }

    return this.get<{ id: number; role: Role; companyName: string }[]>('api/fsps/minimal/get-bsps-with-registered-scheduling-units', {
      params: { productId },
    }).pipe(map(response => response.map(({ id, companyName, role }) => ({ id, companyName, value: id, label: companyName, role }))));
  }

  getDefaultDeliveryPeriod(
    data: Partial<AuctionCmvcDTO | AuctionDayAheadDTO> = {},
    bid: Partial<AuctionOfferDTO> | undefined,
    auctionType: AuctionType
  ): {
    period: Date;
    periodFrom: Date;
    periodTo: Date;
  } {
    let defautlDeliveryPeriod;
    let defautlDeliveryPeriodFrom;
    let defautlDeliveryPeriodTo;

    if (auctionType === AuctionType.CMVC) {
      const auction = data as AuctionCmvcDTO;

      defautlDeliveryPeriod = auction.deliveryDateFrom
        ? moment(auction.deliveryDateFrom).startOf('day').toDate()
        : moment().startOf('day').add(1, 'd').toDate();

      defautlDeliveryPeriodFrom = auction.deliveryDateFrom
        ? moment(auction.deliveryDateFrom).toDate()
        : moment(defautlDeliveryPeriod).startOf('day').toDate();

      defautlDeliveryPeriodTo = auction.deliveryDateTo
        ? moment(auction.deliveryDateTo).toDate()
        : moment(defautlDeliveryPeriod).add(1, 'day').toDate();
    } else {
      const auction = data as AuctionDayAheadDTO;
      const isCapacity = bid?.type
        ? bid.type === AuctionDayAheadType.CAPACITY
        : [AuctionStatus.NEW_CAPACITY, AuctionStatus.OPEN_CAPACITY, AuctionStatus.CLOSED_CAPACITY].includes(auction.status);

      const dateFrom = isCapacity ? auction.capacityAvailabilityFrom : auction.energyAvailabilityFrom;
      const dateTo = isCapacity ? auction.capacityAvailabilityTo : auction.energyAvailabilityTo;

      defautlDeliveryPeriod = dateFrom ? moment(dateFrom).startOf('day').toDate() : moment().startOf('day').add(1, 'd').toDate();

      defautlDeliveryPeriodFrom = dateFrom ? moment(dateFrom).toDate() : moment(defautlDeliveryPeriod).startOf('day').toDate();

      defautlDeliveryPeriodTo = dateTo ? moment(dateTo).toDate() : moment(defautlDeliveryPeriod).add(1, 'day').toDate();
    }

    return {
      period: defautlDeliveryPeriod,
      periodFrom: defautlDeliveryPeriodFrom,
      periodTo: defautlDeliveryPeriodTo,
    };
  }

  getFsps(productId?: number): Observable<Dictionary<number>[]> {
    if (!productId) {
      return of([]);
    }

    return this.get<{ id: number; role: Role; companyName: string }[]>('api/fsps/minimal/get-fsps-with-registered-potentials', {
      params: { productId },
    }).pipe(map(response => response.map(({ id, companyName, role }) => ({ id, companyName, value: id, label: companyName, role }))));
  }

  getFlexPotentials(auctionCmvcId?: number): Observable<Array<FlexPotentialMinimalDTO & { value: string }>> {
    if (!auctionCmvcId) {
      return of([]);
    }

    return this.get<FlexPotentialMinimalDTO[]>('api/user/auctions-cmvc/get-all-registered-fp-for-auction', {
      params: { auctionCmvcId },
    }).pipe(
      map((data: FlexPotentialMinimalDTO[]) =>
        data.map((potential: FlexPotentialMinimalDTO) => ({
          ...potential,
          value: 'auctions.offers.fspPotentialInfo',
        }))
      )
    );
  }

  getSelfScheduleDetail(derId: number, selfScheduleDate: string | undefined): Observable<SelfScheduleVolumesDTO[]> {
    return this.get(`api/user/self-schedule/get-volumes-for-der`, {
      params: { derId, selfScheduleDate },
    });
  }

  getSelfScheduleDetailForAllDers(ders: number[], selfScheduleDate: string | undefined): Observable<AllSelfScheduleVolumesDTO> {
    return this.get(`api/user/self-schedule/get-volumes-for-ders`, {
      params: { ders, selfScheduleDate },
    });
  }

  getAllSelfScheduleDetail(offerId: number): Observable<AllSelfScheduleVolumesDTO> {
    return this.get(`api/user/self-schedule/get-volumes-for-offer`, {
      params: { offerId },
    });
  }

  getSchedulingUnits(productId?: number): Observable<Array<FlexPotentialMinimalDTO & { value: string }>> {
    if (!productId) {
      return of([]);
    }

    return this.get<FlexPotentialMinimalDTO[]>('api/user/scheduling-units/minimal/get-all-registered-for-bsp-and-product', {
      params: { productId },
    }).pipe(
      map((data: FlexPotentialMinimalDTO[]) =>
        data.map((potential: FlexPotentialMinimalDTO) => ({
          ...potential,
          type: this.translate.instant(potential.schedulingUnitType?.nlsCode),
          value: 'auctions.offers.bspPotentialInfo',
          dersData: (potential.ders || []).map(({ name, sourcePower }) => `${name}(${sourcePower} kW)`).join(', '),
        }))
      )
    );
  }

  getStatusForAuctionOfferType(type: AuctionDayAheadType, statusType: 'open'): AuctionStatus[] {
    let statusByType: AuctionStatus | undefined;

    switch (statusType) {
      case 'open':
        statusByType = type === AuctionDayAheadType.CAPACITY ? AuctionStatus.OPEN_CAPACITY : AuctionStatus.OPEN_ENERGY;

        return [AuctionStatus.OPEN, statusByType];
    }
  }

  getOffer(id: number): Observable<AuctionOfferDTO> {
    return this.get(`${this.url}/${id}`);
  }

  save(data: AuctionOfferDTO, auction: AuctionOfferTypeDTO): Observable<void> {
    return this.post(`${this.url}`, this.formatData(data, auction), { context: CUSTOM_HANDLE_ERROR_CONTEXT });
  }

  update(data: AuctionOfferDTO, auction: AuctionOfferTypeDTO): Observable<void> {
    return this.put(`${this.url}`, this.formatData(data, auction), { context: CUSTOM_HANDLE_ERROR_CONTEXT });
  }

  downloadTemplate(scheduleUnitId: number, auctionId: number) {
    this.get<FileDTO>(`api/user/auctions-day-ahead/${auctionId}/offer-template`, {
      params: { schedulingUnitId: scheduleUnitId },
    }).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.XLSX] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  importBid(
    schedulingUnitId: number,
    auctionId: number,
    file: FormData,
    bidId?: number,
    deliveryPeriod?: { deliveryPeriodFrom: string; deliveryPeriodTo: string }
  ) {
    return this.post(`api/user/auctions-day-ahead/${auctionId}/offer-import`, file, {
      params: { schedulingUnitId, offerId: bidId, ...this.formatDateTime(deliveryPeriod, ['deliveryPeriodFrom', 'deliveryPeriodTo']) },
    });
  }

  updateFspValidatorsForCmvc(fspVolume: number, form: UntypedFormGroup) {
    if (!!fspVolume && fspVolume < this.maximumAllowedVolume && form) {
      form.get('volume')!.removeValidators(Validators.max(this.maximumAllowedVolume));
      this.maximumAllowedVolume = fspVolume;
      form.get('volume')!.addValidators(Validators.max(this.maximumAllowedVolume));
      form.get('volume')!.updateValueAndValidity();
    }
    return form;
  }

  private formatToDate(value: string | undefined, defaultDate?: Date): Date | null {
    return value ? moment(value).toDate() : defaultDate || null;
  }

  private formatData(data: AuctionOfferDTO, auction: AuctionOfferTypeDTO): AuctionOfferDTO {
    const { acceptedVolume, acceptedDeliveryPeriodFrom, acceptedDeliveryPeriodTo, ...form } = data;
    let formData: Partial<AuctionOfferDTO> = { ...form };

    if (!!data.id) {
      formData = {
        ...formData,
        acceptedVolume,
        acceptedDeliveryPeriodFrom,
        acceptedDeliveryPeriodTo,
      };
    }

    switch (this.auctionType) {
      case AuctionType.CMVC:
        formData = {
          ...formData,
          auctionCmvc: auction,
          type: AuctionDayAheadType.CAPACITY,
        };
        break;
      case AuctionType.DAY_AHEAD:
        formData = {
          ...formData,
          auctionDayAhead: auction,
          type: auction?.status === AuctionStatus.OPEN_CAPACITY ? AuctionDayAheadType.CAPACITY : AuctionDayAheadType.ENERGY,
        };
        break;
    }

    return this.formatDateTime(formData, [
      'deliveryPeriodFrom',
      'deliveryPeriodTo',
      'acceptedDeliveryPeriodFrom',
      'acceptedDeliveryPeriodTo',
    ]);
  }

  private getAuctionOfferType(
    auctionType: AuctionType,
    bid: Partial<AuctionOfferDTO> | undefined,
    auction: Partial<AuctionCmvcDTO | AuctionDayAheadDTO> = {}
  ): AuctionDayAheadType {
    let type: AuctionDayAheadType = bid && bid.type ? bid.type : AuctionDayAheadType.CAPACITY;

    if (auctionType === AuctionType.DAY_AHEAD) {
      const auctionDa = auction as Partial<AuctionDayAheadDTO>;

      if (!(bid && bid.type)) {
        type = [AuctionStatus.NEW_ENERGY, AuctionStatus.OPEN_ENERGY, AuctionStatus.CLOSED_ENERGY].includes(
          auctionDa.status || AuctionStatus.NEW
        )
          ? AuctionDayAheadType.ENERGY
          : AuctionDayAheadType.CAPACITY;
      }
    }

    return type;
  }

  private getVolumeRangeValidation(auctionType: AuctionType, data: Partial<AuctionOfferDTO>, auction: any) {
    const lowValues: number[] = [];
    const maxValues: number[] = [];

    const productMin = auction.product?.minBidSize ?? null;
    const productMax = auction.product?.maxBidSize ?? null;
    lowValues.push(productMin);
    maxValues.push(productMax);

    let auctionMin = auction.minDesiredPower ?? null;
    let auctionMax = auction.maxDesiredPower ?? null;

    if (auctionType === AuctionType.DAY_AHEAD) {
      const type = [AuctionStatus.NEW_ENERGY, AuctionStatus.OPEN_ENERGY, AuctionStatus.CLOSED_ENERGY].includes(auction.status)
        ? AuctionDayAheadType.ENERGY
        : AuctionDayAheadType.CAPACITY;

      if (type === AuctionDayAheadType.ENERGY) {
        auctionMin = auction.minDesiredEnergy ?? null;
        auctionMax = auction.maxDesiredEnergy ?? null;
      } else {
        auctionMin = auction.minDesiredCapacity ?? null;
        auctionMax = auction.maxDesiredCapacity ?? null;
      }
    }

    lowValues.push(auctionMin);
    maxValues.push(auctionMax);

    if (auctionType === AuctionType.CMVC) {
      const flexRegisterMax = data.flexPotential?.volume ?? null;
      maxValues.push(flexRegisterMax);
    }

    const low = Math.max(...lowValues.filter((value: number | null) => !isNil(value)));
    const max = Math.min(...maxValues.filter((value: number | null) => !isNil(value)));

    this.lowestAllowedVolume = low ?? 0;
    this.maximumAllowedVolume = max ?? 0;

    return [Validators.min(this.lowestAllowedVolume), Validators.max(this.maximumAllowedVolume)];
  }
}
