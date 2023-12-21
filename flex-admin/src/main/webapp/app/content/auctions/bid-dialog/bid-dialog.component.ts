import * as moment from 'moment';

import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup,
  UntypedFormArray,
  UntypedFormControl,
  UntypedFormGroup,
  Validators,
} from '@angular/forms';
import { AppToastrService, AuthService, ToastrMessage } from '@app/core';
import { AuctionBidBandService, IDerBandDTO } from './bid-band.service';
import { AuctionBidService, AuctionOfferTypeDTO } from './bid-dialog.service';
import { AuctionDayAheadType, AuctionStatus, AuctionType } from '../enums';
import { AuctionOfferDTO, AuctionOfferDerDTO } from '../offers/offer';
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  ChangeDetectionStrategy,
} from '@angular/core';
import { Dictionary, FlexPotentialMinimalDTO } from '@app/shared/models';
import { MaxOrSameValidator, MinOrSameValidator } from '@app/shared/commons/validators';
import { Observable, Subject, distinctUntilChanged, of, switchMap, takeUntil, catchError, lastValueFrom } from 'rxjs';
import { debounceTime, startWith } from 'rxjs/operators';

import { AuctionCmvcDTO } from '../cm-vc/cm-vc';
import { AuctionDayAheadDTO } from '../day-ahead/day-ahead';
import { AuctionOfferDerBandDataDTO } from './../offers/offer';
import { AuctionOfferStatus } from './../enums/auction-offer-status';
import { CmVcService } from '../cm-vc/cm-vc.service';
import { DayAheadService } from '../day-ahead/day-ahead.service';
import { Helpers } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { Role } from '@app/shared/enums';
import { AllSelfScheduleVolumesDTO, SelfScheduleVolumesDTO } from '@app/content/units/tabs/self-schedules/self-schedule';
import { TranslateService } from '@ngx-translate/core';
import { UnitMinDTO } from '@app/content/scheduling-units/scheduling-units';
import { isNil } from 'lodash-es';
import { FileUpload } from 'primeng/fileupload';

interface Dictionaries {
  ders: UnitMinDTO[];
  bandNumbers: Dictionary[];
  hourNumbers: Dictionary[];
  companies$: Observable<Dictionary[]>;
  potentials: Array<FlexPotentialMinimalDTO & { value: string }>;
}

interface IBandDataHours {
  bandArrays: FormArray[];
  selfScheduleArray: FormArray[];
}

@Component({
  selector: 'app-auctions-bid',
  templateUrl: './bid-dialog.component.html',
  styleUrls: ['./bid-dialog.component.scss'],
  providers: [AuctionBidService, AuctionBidBandService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuctionBidComponent implements OnInit, OnDestroy {
  readonly MAX_BAND_NUMBER = 10;
  auctionData: AuctionCmvcDTO | AuctionDayAheadDTO | undefined;
  bidData: AuctionOfferDTO | undefined;

  @Input() type: AuctionType = AuctionType.CMVC;
  @Input() auctionId: number | undefined;
  @Input() bidId: number | undefined;
  @Input() isFromBids = false;

  @Output() closeAuctionBid = new EventEmitter<void>();

  @ViewChild(FileUpload) fileUploadEl: FileUpload | null = null;

  mode: 'add' | 'edit' = 'add';

  dictionaries: Dictionaries = {
    bandNumbers: Array.from({ length: 20 }, (_, index: number) => {
      const value = index >= 10 ? index - 9 : index - 10;

      return {
        value,
        label: value < 0 ? value.toString() : `+${value}`,
      };
    }),
    hourNumbers: [],
    companies$: of([]),
    potentials: [],
    ders: [],
  };

  form!: UntypedFormGroup;

  dersData = [];
  hours: string[] = [];
  minPeriodFrom: Date | undefined;
  maxPeriodTo: Date | undefined;
  volumeRange: { min: number; max: number; acceptedMin: number } = { min: 0, max: 0, acceptedMin: 0 };
  positiveBandsVolumeSum: any = {};
  negativeBandsVolumeSum: any = {};
  selfScheduleSum: any = {};
  hasRoleToAcceptedVolume = false;
  isClosedAuction = false;
  isEmptySelfSchedule: boolean[] = [];
  removedDerBands: any[] = [];
  removedDers: any[] = [];
  bidWasImported = false;
  showPrices = true;

  private selfScheduleData: Array<SelfScheduleVolumesDTO[]> = [];

  get auction(): AuctionOfferTypeDTO | undefined {
    return this.auctionData;
  }

  get cmvcAuction(): AuctionCmvcDTO | undefined {
    return this.auctionType === AuctionType.CMVC ? (this.auctionData as AuctionCmvcDTO) : undefined;
  }

  get dayAheadAuction(): AuctionDayAheadDTO | undefined {
    return this.auctionType === AuctionType.DAY_AHEAD ? (this.auctionData as AuctionDayAheadDTO) : undefined;
  }

  get dersControls(): UntypedFormGroup[] {
    const dersControl = this.form?.get('ders') as UntypedFormArray;

    if (!dersControl) {
      return [];
    }

    return dersControl.controls as UntypedFormGroup[];
  }

  get dersControl(): UntypedFormArray {
    return this.form.get('ders') as UntypedFormArray;
  }

  get commonPriceOfBidControl(): FormControl {
    return this.form.get('commonPriceOfBid') as FormControl;
  }

  get bandNumberControl(): FormControl {
    return this.form.get('bandNumber') as FormControl;
  }

  get commonPriceControl(): FormControl {
    return this.form.get('commonPrice') as FormControl;
  }

  get hourNumberControl(): FormControl {
    return this.form.get('hourNumber') as FormControl;
  }

  get deliveryPeriodFromControl(): FormControl {
    return this.form.get('deliveryPeriodFrom') as FormControl;
  }

  get deliveryPeriodToControl(): FormControl {
    return this.form.get('deliveryPeriodTo') as FormControl;
  }

  get acceptedDeliveryPeriodFromControl(): FormControl {
    return this.form.get('acceptedDeliveryPeriodFrom') as FormControl;
  }

  get acceptedDeliveryPeriodToControl(): FormControl {
    return this.form.get('acceptedDeliveryPeriodTo') as FormControl;
  }

  get fromBids(): boolean {
    return this.isFromBids;
  }

  get isOfferPendingOrNotEdit(): boolean {
    return (
      this.bidData?.status === AuctionOfferStatus.PENDING || this.bidData?.status === AuctionOfferStatus.VOLUMES_VERIFIED || !this.isEdit
    );
  }

  get auctionType(): AuctionType {
    return this.type;
  }

  get autionOfferType(): AuctionDayAheadType {
    if (this.auctionType === AuctionType.CMVC) {
      return AuctionDayAheadType.CAPACITY;
    }

    if (!!this.bidData?.type) {
      return this.bidData.type;
    }

    return [AuctionStatus.CLOSED_CAPACITY, AuctionStatus.NEW_CAPACITY, AuctionStatus.OPEN_CAPACITY].includes(
      this.auction?.status || AuctionStatus.NEW
    )
      ? AuctionDayAheadType.CAPACITY
      : AuctionDayAheadType.ENERGY;
  }

  get productId(): number | undefined {
    if (this.auctionType === AuctionType.CMVC) {
      return this.cmvcAuction?.productId ?? this.cmvcAuction?.product?.id;
    }

    return this.dayAheadAuction?.productId ?? this.dayAheadAuction?.product?.id;
  }

  get showBidsTable(): boolean {
    return this.auctionType === AuctionType.DAY_AHEAD;
  }

  get titleParams(): { auctionName: string; bidId?: number } {
    return {
      auctionName: this.auctionData?.name ?? '',
      bidId: this.bidId,
    };
  }

  get canSetAcceptedVolume(): boolean {
    const auctionStatus = this.service.getAuctionStatus(this.auctionType, this.bidData, this.auctionData);

    return !!this.bidData?.volumeDivisibility && this.hasRoleToAcceptedVolume && this.isEdit && auctionStatus === 'CLOSED';
  }

  get isEdit(): boolean {
    return this.mode === 'edit';
  }

  get saveDisabled(): boolean {
    return this.isClosedAuction && this.canSetAcceptedVolume && this.isOfferPendingOrNotEdit
      ? false
      : !(!this.isClosedAuction && !this.isEmptySelfSchedule.includes(true) && this.isOfferPendingOrNotEdit);
  }

  protected destroy$ = new Subject<void>();

  constructor(
    private cdr: ChangeDetectorRef,
    public toastr: AppToastrService,
    private cmvcService: CmVcService,
    private authService: AuthService,
    private service: AuctionBidService,
    private translate: TranslateService,
    private dayAheadService: DayAheadService,
    private bandService: AuctionBidBandService
  ) {}

  ngOnInit(): void {
    this.authService.hasAuthority('FLEX_ADMIN_VIEW_PRICES').then((hasAuthority: boolean) => {
      this.showPrices = hasAuthority;
    });

    this.service.auctionType = this.auctionType;
    this.mode = this.bidId ? 'edit' : 'add';
    if (this.auctionId) {
      if (this.auctionType === AuctionType.DAY_AHEAD) {
        this.dayAheadService.getAuction(this.auctionId).subscribe((model: AuctionDayAheadDTO) => {
          this.auctionData = model;

          this.dictionaries.companies$ = this.service.getBsps(this.productId, this.auction?.id);

          if (this.bidId) {
            this.service.getOffer(this.bidId).subscribe(async (response: AuctionOfferDTO) => {
              this.bidData = response;
              this.getHours();
              this.form = this.service.createForm(response, this.auctionData, this.bidData, this.auctionType, this.fromBids, this.hours);

              this.dersControl.patchValue(this.form.value.ders);
              this.dictionaries.companies$ = this.service.getBsps(this.productId, this.auction?.id);

              await this.getSelfScheduleDataForAllDers(true);

              this.initSubscribe();
              this.getCurrentVolumeRange();
              this.getPotentials(true);

              this.cdr.markForCheck();
            });
          } else {
            this.getHours();

            this.form = this.service.createForm(
              {
                ders: [
                  {
                    bandData: this.bandService.createInitBandData(this.hours, this.auctionData.product.direction, null),
                  },
                ],
              },
              this.auctionData,
              this.bidData,
              this.auctionType,
              this.fromBids,
              this.hours
            );
            this.getCurrentVolumeRange();
            this.initSubscribe();
            this.cdr.markForCheck();
          }

          const auctionIsClosed = [AuctionStatus.CLOSED, AuctionStatus.CLOSED_CAPACITY, AuctionStatus.CLOSED_ENERGY].includes(
            this.auction?.status || AuctionStatus.NEW
          );

          this.authService
            .hasAnyRoles([Role.ROLE_ADMIN, Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR])
            .then(hasRole => {
              this.hasRoleToAcceptedVolume = hasRole;
              this.isClosedAuction = hasRole && auctionIsClosed;
            });
        });
      } else {
        this.cmvcService.getAuction(this.auctionId).subscribe((model: AuctionCmvcDTO) => {
          this.auctionData = model;

          this.dictionaries.companies$ = this.service.getFsps(this.cmvcAuction?.id);

          if (this.bidId) {
            this.service.getOffer(this.bidId).subscribe((response: AuctionOfferDTO) => {
              this.bidData = response;
              this.getHours();

              this.form = this.service.createForm(response, this.auctionData, this.bidData, this.auctionType, this.fromBids, this.hours);
              this.dictionaries.companies$ = this.service.getFsps(this.cmvcAuction?.id);

              this.initSubscribe();
              this.getCurrentVolumeRange();
              this.getPotentials(true);
              this.cdr.markForCheck();
            });
          } else {
            this.getHours();

            this.form = this.service.createForm(undefined, this.auctionData, this.bidData, this.auctionType, this.fromBids, this.hours);
            this.getCurrentVolumeRange();
            this.initSubscribe();
            this.cdr.markForCheck();
          }
          this.cdr.markForCheck();
        });
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  async addDersGroup(all: boolean = false): Promise<void> {
    const dersControl = this.form.get('ders') as UntypedFormArray;
    const hasDers = !!this.dictionaries.ders.length;

    if (all) {
      const availableDers = this.getAvailableDers(-1);

      availableDers.forEach((der: UnitMinDTO) => {
        dersControl.push(
          this.service.createDersGroup(
            hasDers,
            this.auctionType,
            this.bidData,
            this.bidData,
            {
              der,
              bandData: this.bandService.createInitBandData(this.hours, this.auctionData?.product?.direction, null),
            },
            this.auctionData,
            this.hours,
            this.bidWasImported
          )
        );
      });

      const emptyDers = dersControl
        .getRawValue()
        .map(({ der }, index: number) => ({ der, index }))
        .filter(({ der }) => !der)
        .map(({ index }) => index);

      emptyDers.reverse().forEach((index: number) => {
        dersControl.removeAt(index);
      });

      const allDersId = availableDers.map(item => item.id);

      this.getSelfScheduleDataForAllDers(true, allDersId);
      return;
    }

    dersControl.push(
      this.service.createDersGroup(
        hasDers,
        this.auctionType,
        this.bidData,
        this.bidData,
        { bandData: this.bandService.createInitBandData(this.hours, this.auctionData?.product?.direction, null) },
        this.auctionData,
        this.hours,
        this.bidWasImported
      )
    );
  }

  addDersBand(bandDataItem: IDerBandDTO, derIndex: number): void {
    const bandDataArray = this.dersControl.at(derIndex).get('bandData') as FormArray;
    const newBandNumber = bandDataItem.bandNumber > 0 ? bandDataItem.bandNumber + 1 : bandDataItem.bandNumber - 1;

    if (newBandNumber > this.MAX_BAND_NUMBER || newBandNumber < -this.MAX_BAND_NUMBER) {
      return;
    }

    const limitExceedValues: any = {
      der: {
        sourcePower: bandDataItem?.data[0]?.sourcePower,
        pmin: bandDataItem?.data[0]?.pmin,
      },
      selfSchedule: [],
    };

    const bandInitData = this.hours.map(hourNumber => {
      limitExceedValues.selfSchedule.push({
        hourNumber,
        volume: bandDataItem?.data?.find(item => item.hourNumber === hourNumber)?.selfSchedule,
      });
      return {
        hourNumber,
        bandNumber: newBandNumber,
        volume: null,
        acceptedVolume: null,
        acceptedPrice: null,
        price: null,
        isEdited: false,
      };
    });

    const auctionStatus = this.service.getAuctionStatus(this.auctionType, this.bidData, this.auctionData);

    bandDataArray.push(
      this.bandService.createDerBandDataGroup(
        newBandNumber,
        bandInitData,
        this.bidData,
        auctionStatus,
        this.autionOfferType,
        true,
        undefined,
        limitExceedValues
      )
    );
    bandDataArray.controls.sort((a, b) => b.value.bandNumber - a.value.bandNumber);

    this.toggleBandFieldAvailability();
    this.toggleBandFieldAvailability(undefined, true);
  }

  applyCommonPrice(): void {
    this.bandNumberControl?.markAsTouched();
    this.commonPriceControl?.markAsTouched();
    this.hourNumberControl?.markAsTouched();

    const bandNumber = this.bandNumberControl.value?.value;
    const hourNumber = this.hourNumberControl.value;
    const value = this.commonPriceControl.value;

    if (hourNumber === null || value === null) {
      return;
    }

    if (this.dayAheadAuction?.type === AuctionDayAheadType.ENERGY && !bandNumber) {
      return;
    }

    if (this.isClosedAuction) {
      return;
    }

    this.getBandDataHoursFormDers(this.dersControl.controls).bandArrays.forEach(dataArray => {
      dataArray.controls.forEach(dataGroup => {
        const priceControl = dataGroup.get('price') as FormControl;

        if (dataGroup.value.bandNumber === 0) {
          return;
        }

        if (dataGroup.value.volume === null || dataGroup.value.hourNumber !== hourNumber) {
          return;
        }

        if (this.dayAheadAuction?.type === AuctionDayAheadType.ENERGY && dataGroup.value.bandNumber !== bandNumber) {
          return;
        }

        priceControl.patchValue(value);
      });
    });

    this.toggleBandFieldAvailability();
  }

  removeDersBand(bandDataItem: IDerBandDTO, derIndex: number, bandIndex: number): void {
    const correctBandNumberInData =
      this.mode === 'edit' && !this.isClosedAuction && !this.canSetAcceptedVolume && this.isOfferPendingOrNotEdit;

    if (this.mode !== 'add' && this.isClosedAuction && this.canSetAcceptedVolume) {
      const removedBand = {
        derIndex,
        bandData: {
          ...bandDataItem,
          data: {
            ...bandDataItem.data,
            acceptedVolume: null,
            acceptedPrice: null,
          },
        },
      };

      const foundBand = this.removedDerBands.find(
        band => band.bandData.bandNumber === removedBand.bandData.bandNumber && band.derIndex === removedBand.derIndex
      );

      if (!foundBand) {
        this.removedDerBands.push(removedBand);
      }
    }

    const bandDataControl = this.dersControl.at(derIndex).get('bandData') as FormArray;
    bandDataControl.removeAt(bandIndex);

    this.bandService.correctBandsNumberAfterRemoveBand(bandDataControl, bandDataItem.bandNumber, bandIndex, correctBandNumberInData);
  }

  checkIsVolumeValid(volume: number | null, volumeControl: UntypedFormControl): void {
    if (volume === null || !(volume > this.volumeRange.min && volume < this.volumeRange.max)) {
      volumeControl?.markAsDirty();
      volumeControl?.markAsTouched();

      if (volume === null) {
        volumeControl?.setValue(null);
      } else if (!(volume > this.volumeRange.min && volume < this.volumeRange.max)) {
        volumeControl?.setValue(volume);
      }
    } else if (volume > this.volumeRange.min && volume < this.volumeRange.max) {
      volumeControl?.setValue(volume);
    }

    volumeControl?.updateValueAndValidity();

    let acceptedVolumeControl: AbstractControl | null;

    if (this.auctionType === AuctionType.DAY_AHEAD) {
      const parent = volumeControl.parent as UntypedFormGroup;

      acceptedVolumeControl = parent.get('acceptedVolume');
    } else {
      acceptedVolumeControl = this.form?.get('acceptedVolume');
    }

    if (acceptedVolumeControl?.enable) {
      acceptedVolumeControl.setValidators([MinOrSameValidator(this.volumeRange.acceptedMin), MaxOrSameValidator(volume || 0)]);

      acceptedVolumeControl.updateValueAndValidity();
    }
  }

  checkSelectedPotential(): void {
    const selectedPotential = this.form?.get('flexPotential')?.value;
    this.form = this.service.updateFspValidatorsForCmvc(selectedPotential?.volume, this.form);
    this.getCurrentVolumeRange();
  }

  async derChange(index: number, imported = false): Promise<void> {
    const derValue = this.dersControl.at(index).get('der')?.value;
    const bandDataHours = this.getBandDataHoursFormDers([this.dersControl.controls[index]], true);
    this.isEmptySelfSchedule[index] = false;

    if (derValue?.id) {
      const selfScheduleDetails = await lastValueFrom(this.service.getSelfScheduleDetail(derValue.id, this.auctionData?.deliveryDate));
      this.selfScheduleData[index] = selfScheduleDetails;

      if (selfScheduleDetails?.length) {
        bandDataHours.selfScheduleArray[0].patchValue(this.mapSelfScheduleVolumes(selfScheduleDetails));
      } else {
        this.isEmptySelfSchedule[index] = true;
      }
    } else {
      bandDataHours.selfScheduleArray[0].patchValue(
        this.hours.map(hour => ({ hourNumber: hour, bandNumber: 0, acceptedVolume: null, price: null, volume: null }))
      );
    }

    this.dersControl.at(index).get('derDisabled')?.setValue(derValue);
    if (!this.isEmptySelfSchedule[index] && !imported) {
      bandDataHours.bandArrays.forEach(dataArray => {
        dataArray.controls.forEach(dataGroup => {
          if (dataGroup.value.bandNumber !== 0) {
            dataGroup.get('price')?.setValue(null);
          }

          dataGroup.get('volume')?.setValue(null);

          const hourNumber = dataGroup.get('hourNumber')?.value;
          this.selfScheduleData.forEach(selfData => {
            const selfSchedule = selfData.find(item => item.id === hourNumber)?.value;
            (dataGroup as FormGroup).patchValue({ selfSchedule });
          });
          (dataGroup as FormGroup).patchValue({ pmin: derValue?.pmin });
          (dataGroup as FormGroup).patchValue({ sourcePower: derValue?.sourcePower });
        });
        if (derValue) {
          dataArray.enable();
        } else {
          dataArray.disable();
        }
      });
    }

    if (this.commonPriceControl.getRawValue()) {
      this.commonPriceControl.updateValueAndValidity({ onlySelf: true, emitEvent: true });
    }
    this.dersControl.updateValueAndValidity({ onlySelf: true, emitEvent: true });
    this.toggleBandFieldAvailability();
    this.toggleBandFieldAvailability(undefined, true);
  }

  getAvailableDers(derIndex: number): UnitMinDTO[] {
    const ders = this.dersControl.getRawValue() ?? [];
    const auctionStatus = this.auction?.status as AuctionStatus;
    const { CLOSED, CLOSED_CAPACITY, CLOSED_ENERGY } = AuctionStatus;
    if (this.bidId && [CLOSED, CLOSED_CAPACITY, CLOSED_ENERGY].includes(auctionStatus)) {
      return [ders.filter((_, index: number) => index === derIndex)[0]?.der];
    }
    const selectedDers = ders.filter((_, index: number) => index !== derIndex).map(({ der }) => der?.id);

    return this.dictionaries.ders.filter(({ id }) => !selectedDers.includes(id));
  }

  getControl(formGroup: any, key: string): UntypedFormControl {
    return formGroup.get(key) as UntypedFormControl;
  }

  getCurrentVolumeRange(): void {
    this.volumeRange = this.service.getAllowedVolumeRange();
  }

  getChangedTooltipInformation(data: FormGroup, type: 'price' | 'volume'): string {
    if (!this.isClosedAuction || data.controls.bandNumber.value === 0) {
      return '';
    }

    let changed = false;
    let originalValue = null;

    if (type === 'volume') {
      originalValue = data.controls.volume.value;
      changed = data.controls.acceptedVolume.value !== data.controls.volume.value;
    } else {
      originalValue = data.controls.price.value;
      changed = data.controls.acceptedPrice.value !== data.controls.price.value;
    }

    if (changed && originalValue !== null) {
      return this.translate.instant('auctions.tooltip.changedValue', { value: originalValue, unit: this.getUnit(type) });
    }

    return '';
  }

  getUnit(type: 'price' | 'volume'): string {
    switch (this.dayAheadAuction?.type) {
      case AuctionDayAheadType.CAPACITY:
        return type === 'price' ? 'PLN/kW' : 'kW';
      default:
        return type === 'price' ? 'PLN/kWh' : 'kWh';
    }
  }

  removeDer(index: number): void {
    if (this.bidData) {
      if (this.bidData.ders && this.bidData.ders[index]) {
        const removedDer = this.bidData.ders[index];
        removedDer.bandData?.forEach(bandData => {
          bandData.acceptedPrice = null;
          bandData.acceptedVolume = null;
        });
        if (this.removedDers.length) {
          const foundDer = this.removedDers.find(der => der.der.id === removedDer.der?.id);
          if (!foundDer) {
            this.removedDers.push(removedDer);
          }
        } else {
          this.removedDers.push(removedDer);
        }
      }
    }
    this.dersControl.removeAt(index);
    this.selfScheduleData = this.selfScheduleData.filter((_, dataIndex: number) => dataIndex !== index);
    this.isEmptySelfSchedule = this.isEmptySelfSchedule.filter((_, dataIndex: number) => dataIndex !== index);
  }

  save(): void {
    let method: Observable<void>;
    if (this.auctionType === AuctionType.DAY_AHEAD) {
      this.toggleBandFieldAvailability();
    }

    if (!this.form) {
      return;
    }

    if ((this.isClosedAuction && !this.canSetAcceptedVolume) || !this.isOfferPendingOrNotEdit) {
      return;
    }

    if (this.form.invalid || this.isEmptySelfSchedule.includes(true)) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    if (this.isEmptySelfSchedule.includes(true)) {
      return;
    }

    const { deliveryPeriod, acceptedDeliveryPeriod, fsp, commonPriceOfBid, commonPrice, bandNumber, ...formData } = this.form.getRawValue();

    formData.ders = formData.ders?.map((derItem: { bandData: IDerBandDTO[] | AuctionOfferDerBandDataDTO[] }) => {
      derItem.bandData = (derItem.bandData as IDerBandDTO[])
        ?.map(band => band.data)
        ?.flat()
        .filter(
          bandData =>
            (this.showPrices ? !isNil(bandData.price) && !isNil(bandData.volume) : !isNil(bandData.volume)) ||
            (!isNil(bandData.volume) && bandData.bandNumber === 0)
        );
      return derItem;
    });

    const data = {
      ...this.bidData,
      ...formData,
    };

    if (this.removedDerBands.length && this.mode !== 'add' && this.isClosedAuction && this.canSetAcceptedVolume) {
      this.removedDerBands.forEach(band => {
        data.ders.filter((der: any, idx: number) => {
          const dersCondtiional = idx === band.derIndex;
          if (dersCondtiional) {
            const bandExists = der.bandData.find((bData: AuctionOfferDerBandDataDTO) => band.bandData.bandNumber === bData.bandNumber);
            if (!bandExists) {
              der.bandData = der.bandData.concat(band.bandData.data);
            }
          }
          return dersCondtiional;
        });
      });
    }

    if (this.removedDers.length && this.mode !== 'add' && this.isClosedAuction && this.canSetAcceptedVolume) {
      this.removedDers.forEach(removedDer => {
        const derExists = data.ders.find((der: AuctionOfferDerDTO) => der.der?.id === removedDer.der.id);
        if (!derExists) {
          data.ders = data.ders.concat(this.removedDers);
        }
      });
    }

    if (this.auctionType === 'DAY_AHEAD') {
      data.commonPrice = this.autionOfferType === AuctionDayAheadType.CAPACITY ? commonPrice : null;
    }

    data.ders = data.ders?.map((derItem: { bandData: AuctionOfferDerBandDataDTO[] }) => {
      derItem.bandData = derItem.bandData.filter(
        bandData => !isNil(bandData.volume) || (!isNil(bandData.volume) && bandData.bandNumber === 0)
      );
      return derItem;
    });

    method = this.mode === 'add' ? this.service.save(data, this.auction) : this.service.update(data, this.auction);

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && response.error?.errorKey) {
            if (response.error?.errorKey === 'error.dayAhead.offer.notSetRequiredBandsInEnergyOffer') {
              this.toastr.warning(response.error?.errorKey);
            } else {
              this.toastr.error(response.error?.errorKey);
            }
            return;
          }

          this.toastr.error(new ToastrMessage({ msg: `auctions.actions.bids.${this.mode}.error` }));
        })
      )
      .subscribe(() => {
        this.toastr.success(new ToastrMessage({ msg: `auctions.actions.bids.${this.mode}.success` }));
        this.closeAuctionBid.emit();
      });
  }

  cancel(): void {
    window.close();
  }

  private async getSelfScheduleDataForAllDers(toggleBandFieldAvailability: boolean = false, ders: number[] | null = null): Promise<void> {
    const dersControl = this.form.get('ders') as UntypedFormArray;
    let selfScheduleDetails: AllSelfScheduleVolumesDTO;

    if (ders) {
      selfScheduleDetails = await lastValueFrom(this.service.getSelfScheduleDetailForAllDers(ders, this.auctionData?.deliveryDate));
    } else if (this.bidId) {
      selfScheduleDetails = await lastValueFrom(this.service.getAllSelfScheduleDetail(this.bidId));
    }

    const allSelfScheduleDetails: SelfScheduleVolumesDTO[][] = [];
    dersControl.controls.forEach(control => {
      const data = control.getRawValue();
      allSelfScheduleDetails.push(selfScheduleDetails[data.der.id]);
    });

    allSelfScheduleDetails.forEach((selfScheduleDetails, index) => {
      if (selfScheduleDetails?.length) {
        this.selfScheduleData[index] = selfScheduleDetails;
        const firstPlusBandIndex = this.form.get(`ders.${index}.bandData`)?.value.findIndex((item: any) => item.bandNumber === 1);
        const firstMinusBandIndex = this.form.get(`ders.${index}.bandData`)?.value.findIndex((item: any) => item.bandNumber === -1);
        const plusBandDataArray = this.form.get(`ders.${index}.bandData.${firstPlusBandIndex}.data`) as FormGroup;
        const minusBandDataArray = this.form.get(`ders.${index}.bandData.${firstMinusBandIndex}.data`) as FormGroup;

        if (plusBandDataArray?.controls) {
          Object.keys(plusBandDataArray.controls).forEach((key, idx) => {
            plusBandDataArray.controls[key].patchValue({ selfSchedule: selfScheduleDetails[idx].value });
          });
        }
        if (minusBandDataArray?.controls) {
          Object.keys(minusBandDataArray.controls).forEach((key, idx) => {
            minusBandDataArray.controls[key].patchValue({ selfSchedule: selfScheduleDetails[idx].value });
          });
        }

        this.isEmptySelfSchedule[index] = false;
        this.getBandDataHoursFormDers([this.dersControl.controls[index]]).selfScheduleArray[0].patchValue(
          this.mapSelfScheduleVolumes(selfScheduleDetails)
        );
      } else {
        if (!this.selfScheduleData[index]?.length) {
          this.isEmptySelfSchedule[index] = true;
        }
      }
    });

    if (toggleBandFieldAvailability) {
      this.toggleBandFieldAvailability(true);
      this.toggleBandFieldAvailability(false, true);
    }
  }

  private getPotentials(init: boolean = false): void {
    if (this.auctionType === AuctionType.CMVC) {
      this.service.getFlexPotentials(this.bidData?.flexPotential?.fsp?.id, this.cmvcAuction?.id).subscribe(response => {
        if (response.findIndex(item => item.id === this.bidData?.flexPotential.id) <= -1) {
          response.push({
            ...this.bidData?.flexPotential,
            value: 'auctions.offers.fspPotentialInfo',
          });
        }
        this.dictionaries.potentials = response;
        this.cdr.markForCheck();
      });
    } else {
      this.service.getSchedulingUnits(this.bidData?.schedulingUnit?.bsp?.id, this.productId).subscribe(response => {
        this.dictionaries.potentials = response;

        if (!init) {
          this.cdr.markForCheck();
          return;
        }

        const selected = response.find(({ id }) => id === this.bidData?.schedulingUnit?.id);

        this.dictionaries.ders = selected?.ders ?? [];
        this.cdr.markForCheck();
      });
    }
  }

  private getHours(): void {
    const deliveryPeriod = moment(this.service.getDefaultDeliveryPeriod(this.auction, this.bidData, this.auctionType).period);
    const changeTime = Helpers.checkChangeTime(deliveryPeriod.startOf('day'), deliveryPeriod.endOf('day'));
    this.hours = Helpers.getHours(changeTime);
  }

  private initSubscribe(): void {
    const { periodFrom, periodTo } = this.service.getDefaultDeliveryPeriod(this.auctionData, this.bidData, this.auctionType);

    this.minPeriodFrom = periodFrom;
    this.maxPeriodTo = periodTo;

    this.subbscribeFsp();

    if (this.auctionType === AuctionType.DAY_AHEAD) {
      this.subscribeSchedulingUnit();
      this.subscribeCommonPriceInput();
      this.subscribeAcceptedDeliveryPeriod();
      this.subscribeDeliveryPeriod();
      this.toggleBandFieldAvailability();
      this.toggleBandFieldAvailability(undefined, true);
      this.subscribeBandDataInput();
    }

    if (this.mode === 'edit') {
      this.subscribeDivisibility();
    }
  }

  private methodOnControl(key: string, execute: (control: UntypedFormControl) => void): void {
    if (this.auctionType === AuctionType.DAY_AHEAD) {
      this.service.controlMethodInsideFormArray(this.dersControl, key, (control: UntypedFormControl) => execute(control));
    } else {
      const control = this.form.get(key) as UntypedFormControl;

      execute(control);
    }
  }

  private subscribeDivisibility(): void {
    this.form
      ?.get('volumeDivisibility')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((volumeDivisibility: boolean) => {
        const auctionIsClosed = [AuctionStatus.CLOSED, AuctionStatus.CLOSED_CAPACITY, AuctionStatus.CLOSED_ENERGY].includes(
          this.auction?.status || AuctionStatus.NEW
        );

        this.methodOnControl('acceptedVolume', (control: UntypedFormControl) => {
          if (auctionIsClosed && volumeDivisibility) {
            control?.enable();
          } else {
            const parent = control.parent as UntypedFormGroup;

            control?.setValue(parent?.get('volume')?.value);
            control?.disable();
          }
        });
      });

    this.form
      ?.get('deliveryPeriodDivisibility')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((deliveryPeriodDivisibility: boolean) => {
        const auctionIsClosed = [AuctionStatus.CLOSED, AuctionStatus.CLOSED_CAPACITY, AuctionStatus.CLOSED_ENERGY].includes(
          this.auction?.status || AuctionStatus.NEW
        );

        if (auctionIsClosed && deliveryPeriodDivisibility && this.isOfferPendingOrNotEdit) {
          this.form?.get('acceptedDeliveryPeriodFrom')?.enable();
          this.form?.get('acceptedDeliveryPeriodTo')?.enable();
        } else {
          this.form?.get('acceptedDeliveryPeriodFrom')?.disable();
          this.form?.get('acceptedDeliveryPeriodTo')?.disable();
        }
      });
  }

  private subscribeAcceptedDeliveryPeriod(): void {
    this.acceptedDeliveryPeriodFromControl.valueChanges
      .pipe(takeUntil(this.destroy$), debounceTime(500), distinctUntilChanged())
      .subscribe(() => {
        this.toggleBandFieldAvailability(undefined, true);
      });

    this.acceptedDeliveryPeriodToControl.valueChanges
      .pipe(takeUntil(this.destroy$), debounceTime(500), distinctUntilChanged())
      .subscribe(() => {
        this.toggleBandFieldAvailability(undefined, true);
      });
  }

  private subscribeDeliveryPeriod(): void {
    this.deliveryPeriodFromControl.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(500), distinctUntilChanged()).subscribe(() => {
      this.toggleBandFieldAvailability();
    });

    this.deliveryPeriodToControl.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(500), distinctUntilChanged()).subscribe(() => {
      this.toggleBandFieldAvailability();
    });
  }

  private subscribeBandDataInput(): void {
    this.dersControl.valueChanges.pipe(startWith(this.dersControl.value), takeUntil(this.destroy$), debounceTime(800)).subscribe(() => {
      this.positiveBandsVolumeSum = {};
      this.negativeBandsVolumeSum = {};
      this.selfScheduleSum = {};
      this.dersControl.getRawValue().forEach((item: { bandData: any[] }) => {
        item.bandData?.forEach(bandDataItem => {
          bandDataItem.data?.forEach((dataItem: { bandNumber: number; hourNumber: number; volume: number; acceptedVolume: number }) => {
            if (dataItem.bandNumber > 0) {
              this.positiveBandsVolumeSum[dataItem.hourNumber] =
                (this.positiveBandsVolumeSum[dataItem.hourNumber] || 0) +
                (!this.isClosedAuction ? dataItem.volume : dataItem.acceptedVolume);
            }
            if (dataItem.bandNumber < 0) {
              this.negativeBandsVolumeSum[dataItem.hourNumber] =
                (this.negativeBandsVolumeSum[dataItem.hourNumber] || 0) +
                (!this.isClosedAuction ? dataItem.volume : dataItem.acceptedVolume);
            }
            if (dataItem.bandNumber === 0) {
              this.selfScheduleSum[dataItem.hourNumber] = (this.selfScheduleSum[dataItem.hourNumber] || 0) + dataItem.volume;
            }
          });
        });
      });
      this.cdr.markForCheck();
    });
  }

  private subscribeCommonPriceInput(): void {
    this.commonPriceOfBidControl.valueChanges.pipe(takeUntil(this.destroy$), distinctUntilChanged()).subscribe(value => {
      if (value) {
        this.commonPriceControl.setValidators([Validators.required]);
        this.hourNumberControl.setValidators([Validators.required]);

        if (this.dayAheadAuction?.type === AuctionDayAheadType.ENERGY) {
          this.bandNumberControl.setValidators([Validators.required]);
        }
      } else {
        this.commonPriceControl.setValidators([]);
        this.bandNumberControl.setValidators([]);
        this.hourNumberControl.setValidators([]);

        this.commonPriceControl.setValue(null);
        this.bandNumberControl.setValue(null);
        this.hourNumberControl.setValue(null);
      }

      this.toggleBandFieldAvailability();
      this.commonPriceControl.updateValueAndValidity();
    });

    this.bandNumberControl.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.commonPriceControl.setValue(null);
    });
  }

  private subbscribeFsp(): void {
    this.form
      ?.get('fsp')
      ?.valueChanges.pipe(
        takeUntil(this.destroy$),
        switchMap(fsp => {
          if (this.auctionType === AuctionType.CMVC) {
            this.form.get('flexPotential')?.setValue(null);
            return this.service.getFlexPotentials(fsp?.id, this.cmvcAuction?.id);
          }

          this.form.get('schedulingUnit')?.setValue(null);

          return this.service.getSchedulingUnits(fsp?.id, this.productId);
        })
      )
      .subscribe(response => {
        this.dictionaries.potentials = response;
      });
  }

  private subscribeSchedulingUnit(): void {
    this.form
      .get('schedulingUnit')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((schedulingUnit: any) => {
        this.dictionaries.ders = schedulingUnit?.ders ?? [];

        Helpers.removeExcessFormGroup(this.dersControl, 0);
        this.addDersGroup();

        if (this.dictionaries.ders.length) {
          this.dersControls.forEach((control: UntypedFormGroup) => {
            control.get('der')?.enable();
          });
        } else {
          this.dersControls.forEach((control: UntypedFormGroup) => {
            control.get('der')?.disable();
            control.get('der')?.setValue(null);
          });
        }

        this.cdr.markForCheck();
      });
  }

  private preparedHourNumbers(from: number, to: number): void {
    this.dictionaries.hourNumbers = this.hours
      .filter((hour: string, index: number) => index > from && to >= index)
      .map((hour: string) => ({
        value: hour,
        label: hour,
      }));
  }

  private toggleBandFieldAvailability(setSelfSchedule: boolean = true, volumeDivisibility: boolean = false): void {
    const auctionStatus = this.service.getAuctionStatus(this.auctionType, this.bidData, this.auctionData);
    const auctionIsClosed = auctionStatus === 'CLOSED';

    const periodFromHour = moment(this.deliveryPeriodFromControl.value).format('H');
    const periodToHour = String(Number(moment(this.deliveryPeriodToControl.value).format('H')) || 24);

    let fromIdx = this.hours.findIndex(hour => hour === periodFromHour);
    let toIdx = this.hours.findIndex(hour => hour === periodToHour);

    this.preparedHourNumbers(fromIdx, toIdx);

    if (volumeDivisibility && (!auctionIsClosed || !this.bidData?.volumeDivisibility)) {
      return;
    }

    if (volumeDivisibility) {
      const acceptedPeriodFromHour = moment(this.acceptedDeliveryPeriodFromControl.value).format('H');
      const acceptedPeriodToHour = String(Number(moment(this.acceptedDeliveryPeriodToControl.value).format('H')) || 24);

      fromIdx = this.hours.findIndex(hour => hour === acceptedPeriodFromHour);
      toIdx = this.hours.findIndex(hour => hour === acceptedPeriodToHour);
    }

    this.getBandDataHoursFormDers(this.dersControl.controls, false, true).bandArrays.forEach(dataArray => {
      const derData = dataArray.parent?.parent?.parent?.getRawValue();
      const derId = derData?.der?.id;
      const derIndex = this.dersControl.getRawValue().findIndex((ders: { der: { id: any } }) => ders?.der?.id === derId);
      const originalDerIndex = this.bidData?.ders?.findIndex(({ der }) => der?.id === derId) ?? -1;

      dataArray.controls.forEach((dataGroup, index) => {
        const dataGroupValue = dataGroup.getRawValue();
        if (index > fromIdx && toIdx >= index && !this.isEmptySelfSchedule[derIndex]) {
          const originalData = this.bidData?.ders?.[originalDerIndex]?.bandData ?? [];
          const bandData = originalData.filter(({ bandNumber }) => bandNumber === dataGroupValue.bandNumber) ?? [];
          const hourData = bandData.find(({ hourNumber }) => hourNumber === dataGroupValue.hourNumber);

          if (dataGroupValue.bandNumber === 0) {
            const { acceptedPrice, acceptedVolume, price, volume } = (dataGroup as FormGroup).controls;
            dataGroup.enable();
            [acceptedPrice, acceptedVolume, price, volume].forEach(control => {
              control.disable();
            });
          }
          if (dataGroupValue.bandNumber !== 0 && this.isOfferPendingOrNotEdit) {
            dataGroup.enable();

            if (!this.bidData?.volumeDivisibility || !auctionIsClosed) {
              dataGroup.get('acceptedVolume')?.disable();
            } else if (volumeDivisibility && this.canSetAcceptedVolume) {
              const acceptedVolumeControl = dataGroup.get('acceptedVolume');
              const acceptedPriceControl = dataGroup.get('acceptedPrice');

              if (acceptedVolumeControl?.value === null && originalDerIndex !== -1) {
                acceptedVolumeControl?.setValue(hourData?.acceptedVolume ?? null);
                acceptedPriceControl?.setValue(hourData?.acceptedPrice ?? null);
              }

              acceptedVolumeControl?.enable();
            }

            if (auctionIsClosed) {
              dataGroup.get('price')?.disable();
            }

            if (
              !(
                this.isClosedAuction &&
                dataGroup.get('acceptedPrice')?.value === null &&
                dataGroup.get('price')?.value !== null &&
                dataGroup.get('bandNumber')?.value !== 0
              )
            ) {
              dataGroup.get('acceptedPrice')?.disable();
            }

            if (!hourData || hourData?.volume === null) {
              dataGroup.get('acceptedVolume')?.disable();
            }
          }

          if (dataGroupValue.bandNumber === 0 && setSelfSchedule) {
            const selfSchedule = this.selfScheduleData[derIndex] ?? [];
            const selfScheduleForHour = selfSchedule.find(({ id }) => id === dataGroupValue.hourNumber);

            if (selfScheduleForHour) {
              dataGroup.patchValue({
                volume: Number(selfScheduleForHour.value),
                acceptedVolume: Number(selfScheduleForHour.value),
              });
            }
          }
        } else {
          dataGroup.disable();

          if (!(index > fromIdx && toIdx >= index)) {
            dataGroup.get('acceptedVolume')?.setValue(null);
            dataGroup.get('acceptedPrice')?.setValue(null);

            dataGroup.get('volume')?.setErrors(null);

            if (volumeDivisibility) {
              dataGroup.get('acceptedPrice')?.setValue(null);
              dataGroup.get('acceptedVolume')?.setValue(null);
            } else {
              dataGroup.get('price')?.setValue(null);
              dataGroup.get('volume')?.setValue(null);
            }
          }
        }
      });
    });
    this.cdr.markForCheck();
  }

  private getBandDataHoursFormDers(
    dersArrayContol: AbstractControl[],
    ignoreValueRequired?: boolean,
    ignoreOnlyBands?: boolean
  ): IBandDataHours {
    const dataArrays: IBandDataHours = {
      bandArrays: [],
      selfScheduleArray: [],
    };
    dersArrayContol.forEach(dersGroup => {
      const derControl = dersGroup.get('der') as FormControl;
      if (derControl.value || ignoreValueRequired) {
        const bandDataArray = dersGroup.get('bandData') as FormArray;
        bandDataArray.controls.forEach(bandGroup => {
          const bandNumberControl = bandGroup.get('bandNumber') as FormControl;
          const dataArray = bandGroup.get('data') as FormArray;
          if (bandNumberControl.value !== 0 || ignoreOnlyBands) {
            dataArrays.bandArrays.push(dataArray);
          } else {
            dataArrays.selfScheduleArray.push(dataArray);
          }
        });
      }
    });
    return dataArrays;
  }

  private mapSelfScheduleVolumes(selfScheduleDetails: SelfScheduleVolumesDTO[]): AuctionOfferDerBandDataDTO[] {
    return selfScheduleDetails.map(volume => ({
      hourNumber: volume.id,
      bandNumber: 0,
      acceptedVolume: null,
      acceptedPrice: null,
      price: null,
      volume: Number(volume.value),
      isEdited: false,
    }));
  }

  downloadTemplate() {
    const selectedSchedulingUnit = this.form.get('schedulingUnit')?.value;
    if (selectedSchedulingUnit.id && this.auctionId) {
      this.service.downloadTemplate(selectedSchedulingUnit.id, this.auctionId);
    }
  }

  importBid({ currentFiles }: any) {
    const selectedSchedulingUnit = this.form.get('schedulingUnit')?.value;
    const dataToImport = new FormData();
    dataToImport.append('file', currentFiles[0]);
    if (this.auctionId) {
      const deliveryPeriodFrom = this.form.get('deliveryPeriodFrom')?.value;
      const deliveryPeriodTo = this.form.get('deliveryPeriodTo')?.value;
      this.service
        .importBid(selectedSchedulingUnit.id, this.auctionId, dataToImport, undefined, {
          deliveryPeriodFrom,
          deliveryPeriodTo,
        })
        .pipe(
          catchError((response: HttpErrorResponse): any => {
            this.fileUploadEl?.clear();
            if (!(response.status === 400 && response.error?.errorKey)) {
              this.toastr.error(`auctions.offers.importBid.error`);
              return;
            }
          })
        )
        .subscribe((response: any) => {
          this.bidWasImported = true;
          this.toastr.success(`auctions.offers.importBid.success`);
          this.fileUploadEl?.clear();
          response.deliveryPeriodFrom = deliveryPeriodFrom ?? response.deliveryPeriodFrom;
          response.deliveryPeriodTo = deliveryPeriodTo ?? response.deliveryPeriodTo;
          this.bidData = response;
          if (this.auctionId && this.bidData) {
            this.bidData.auctionId = this.auctionId;
            this.bidData.schedulingUnit = selectedSchedulingUnit;
          }
          this.getHours();

          Helpers.removeExcessFormGroup(this.dersControl, 0);
          const emptyDers = this.dersControl
            .getRawValue()
            .map(({ der }, index: number) => ({ der, index }))
            .filter(({ der }) => !der)
            .map(({ index }) => index);

          emptyDers.reverse().forEach((index: number) => {
            this.dersControl.removeAt(index);
          });

          const dersData: AuctionOfferDerDTO[] = response.ders && response.ders.length ? response.ders : [{}];

          dersData.map((derData: AuctionOfferDerDTO) =>
            this.dersControl.push(
              this.service.createDersGroup(false, this.auctionType, this.bidData, response, derData, this.auctionData, this.hours, true)
            )
          );
          const derIds = this.dersControl.value.map((item: any) => item.der.id);
          this.getSelfScheduleDataForAllDers(true, derIds);
          this.dersControl.patchValue(this.form.value.ders);
          this.initSubscribe();
        });
    }
  }

  trackByFn(index: number): number {
    return index;
  }

  renderInputNumbers(formControl: UntypedFormControl): boolean {
    const parent = formControl?.parent;
    if (parent?.status === 'DISABLED') {
      return false;
    }
    return true;
  }
}
