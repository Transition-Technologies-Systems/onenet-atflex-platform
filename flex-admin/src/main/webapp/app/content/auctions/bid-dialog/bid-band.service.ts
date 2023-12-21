import { AuctionDayAheadType } from '../enums';
import { AuctionOfferDTO, AuctionOfferDerBandDataDTO } from '../offers/offer';
import { BidDerBandDataFormType, BidDerBandFormType } from './models';
import { FormArray, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { validateBandDataForCapacity, validateBandDataForEnergy } from './validators';

import { AuctionDayAheadDTO } from '../day-ahead/day-ahead';
import { AuctionOfferStatus } from './../enums/auction-offer-status';
import { Injectable } from '@angular/core';
import { ProductDirection } from '@app/shared/enums';
import { valusIsNotZero } from '@app/shared/validators';

export interface IDerBandDTO {
  bandNumber: number;
  data: AuctionOfferDerBandDataDTO[];
}

function validateNoFilledDers(offerType: AuctionDayAheadType): ValidatorFn {
  return (formArray: any): ValidationErrors | null => {
    if (offerType === AuctionDayAheadType.ENERGY) {
      return null;
    }
    const data = formArray.controls.filter((group: any) => group.status !== 'DISABLED');

    const someFilled = data.some((group: any) => {
      const { price, volume } = group.controls;
      return price.value !== null || volume.value !== null;
    });

    data.forEach((group: any) => {
      const { volume } = group.controls;
      const { timestampNoFilled, ...rest } = volume.errors || {};
      if (someFilled) {
        volume.setErrors(Object.keys({ ...rest }).length ? { ...rest } : null);
      } else {
        volume.setErrors({ ...rest, timestampNoFilled: true });
      }
    });

    return null;
  };
}

function validateVolumeLimit(offerType: AuctionDayAheadType): ValidatorFn {
  return (formArray: any): ValidationErrors | null => {
    const data = formArray.controls.filter((group: any) => group.status !== 'DISABLED');
    if (offerType === AuctionDayAheadType.ENERGY) {
      return null;
    }

    data.forEach((group: any) => {
      const { volume, selfSchedule, pmin, sourcePower, bandNumber } = group.controls;
      const { limitExceed, ...rest } = volume.errors || {};
      if (selfSchedule && pmin && sourcePower) {
        if (bandNumber.value > 0) {
          if (selfSchedule.value + volume.value <= sourcePower.value) {
            volume.setErrors(Object.keys({ ...rest }).length ? { ...rest } : null);
          } else {
            volume.setErrors({ ...rest, limitExceed: true });
          }
        } else {
          if (selfSchedule.value - volume.value >= pmin.value) {
            volume.setErrors(Object.keys({ ...rest }).length ? { ...rest } : null);
          } else {
            volume.setErrors({ ...rest, limitExceed: true });
          }
        }
      }
    });

    return null;
  };
}

@Injectable()
export class AuctionBidBandService {
  constructor(private fb: FormBuilder) {}

  addMissingBand(
    bid: Partial<AuctionOfferDTO> | undefined,
    hours: string[],
    auctionIsClosed: boolean,
    data: AuctionOfferDerBandDataDTO[],
    auction: Partial<AuctionDayAheadDTO>,
    commonPrice: number | null | undefined
  ): AuctionOfferDerBandDataDTO[] {
    const auctionDirection = auction.product?.direction;
    let initBands = [1, 0, -1];

    if (!bid?.id || auctionIsClosed) {
      return data;
    }

    switch (auctionDirection) {
      case ProductDirection.UP:
        initBands = [1, 0];
        break;
      case ProductDirection.DOWN:
        initBands = [0, -1];
        break;
    }

    const bandsInData = new Set(data.map(({ bandNumber }) => bandNumber));
    const bandData: AuctionOfferDerBandDataDTO[] = [...data];

    initBands.forEach((bandNumber: number) => {
      if (!bandsInData.has(bandNumber)) {
        hours.forEach(hourNumber => {
          bandData.push({
            hourNumber,
            bandNumber,
            volume: null,
            acceptedVolume: null,
            acceptedPrice: null,
            price: bandNumber === 0 ? null : commonPrice ? commonPrice : null,
            isEdited: false,
          });
        });
      }
    });

    return bandData;
  }

  createDerBandDataGroup(
    bandNumber: number,
    data: AuctionOfferDerBandDataDTO[],
    offer: Partial<AuctionOfferDTO> = {},
    auctionStatus: 'OPEN' | 'CLOSED',
    offerType: AuctionDayAheadType,
    newRow?: boolean,
    isEdit?: boolean,
    limitExceedValues?: any
  ): FormGroup<BidDerBandFormType> {
    const isBandNumber = bandNumber > 0 || bandNumber < 0;
    const auctionIsClosed = auctionStatus === 'CLOSED';
    const bidIsPending = offer?.status === AuctionOfferStatus.PENDING;

    return this.fb.group<BidDerBandFormType>({
      bandNumber: this.fb.nonNullable.control(bandNumber),
      data: this.fb.array(
        data.map((bandData: AuctionOfferDerBandDataDTO, index: number) =>
          this.fb.group<BidDerBandDataFormType>({
            hourNumber: this.fb.nonNullable.control(bandData.hourNumber),
            bandNumber: this.fb.nonNullable.control(bandNumber),
            price: this.fb.control(
              { value: bandData.price, disabled: !newRow || auctionIsClosed },
              isBandNumber
                ? offerType === AuctionDayAheadType.ENERGY
                  ? validateBandDataForEnergy(bandData.hourNumber, bandNumber, index, 'price')
                  : validateBandDataForCapacity(bandData.hourNumber, bandNumber, index, 'price')
                : null
            ),
            volume: this.fb.control(
              { value: bandData.volume, disabled: !newRow || auctionIsClosed },
              isBandNumber
                ? offerType === AuctionDayAheadType.ENERGY
                  ? validateBandDataForEnergy(bandData.hourNumber, bandNumber, index, 'volume')
                  : [validateBandDataForCapacity(bandData.hourNumber, bandNumber, index, 'volume'), valusIsNotZero]
                : null
            ),
            acceptedVolume: this.fb.control(
              {
                value: bandData.acceptedVolume,
                disabled: !offer.volumeDivisibility || !auctionIsClosed || !isBandNumber || !bidIsPending || !isEdit,
              },
              isEdit
                ? offerType === AuctionDayAheadType.ENERGY
                  ? [
                      validateBandDataForEnergy(bandData.hourNumber, bandNumber, index, 'acceptedVolume'),
                      Validators.max(bandData.volume ?? 0),
                    ]
                  : [
                      validateBandDataForCapacity(bandData.hourNumber, bandNumber, index, 'acceptedVolume'),
                      Validators.max(bandData.volume ?? 0),
                    ]
                : null
            ),
            acceptedPrice: this.fb.control(
              { value: bandData.acceptedPrice, disabled: true },
              isEdit
                ? offerType === AuctionDayAheadType.ENERGY
                  ? [
                      validateBandDataForEnergy(bandData.hourNumber, bandNumber, index, 'acceptedPrice'),
                      Validators.max(bandData.price ?? 0),
                    ]
                  : [
                      validateBandDataForCapacity(bandData.hourNumber, bandNumber, index, 'acceptedPrice'),
                      Validators.max(bandData.price ?? 0),
                    ]
                : null
            ),
            isEdited: this.fb.nonNullable.control(bandData.isEdited),
            sourcePower: this.fb.nonNullable.control(limitExceedValues?.der?.sourcePower),
            pmin: this.fb.nonNullable.control(limitExceedValues?.der?.pmin),
            selfSchedule: this.fb.control(
              limitExceedValues?.selfSchedule.find((item: any) => item.hourNumber === bandData.hourNumber)?.volume
            ),
          })
        ),
        { validators: [validateNoFilledDers(offerType), validateVolumeLimit(offerType)] }
      ),
    });
  }

  createInitBandData(
    hours: string[],
    auctionDirection: ProductDirection | undefined,
    commonPrice: number | null
  ): AuctionOfferDerBandDataDTO[] {
    let initBands = [1, 0, -1];
    switch (auctionDirection) {
      case ProductDirection.UP:
        initBands = [1, 0];
        break;
      case ProductDirection.DOWN:
        initBands = [0, -1];
        break;
    }
    const initBandData: AuctionOfferDerBandDataDTO[] = [];
    initBands.forEach(bandNumber => {
      hours.forEach(hourNumber => {
        initBandData.push({
          hourNumber,
          bandNumber,
          volume: null,
          acceptedVolume: null,
          acceptedPrice: null,
          price: bandNumber === 0 ? null : commonPrice,
          isEdited: false,
        });
      });
    });

    return initBandData;
  }

  correctBandsNumberAfterRemoveBand(
    bandDataControl: FormArray<FormGroup<BidDerBandFormType>>,
    removedBandNumber: number,
    bandIndex: number,
    correctData: boolean
  ): void {
    const bandsData = bandDataControl.getRawValue();

    if (removedBandNumber > 0) {
      while (bandIndex > 0) {
        bandIndex--;

        bandsData[bandIndex].bandNumber--;

        if (correctData) {
          bandsData[bandIndex].data.forEach((data: any) => {
            data.bandNumber = bandsData[bandIndex].bandNumber;
          });
        }
      }
    } else {
      while (bandIndex < bandsData.length) {
        bandsData[bandIndex].bandNumber++;

        if (correctData) {
          bandsData[bandIndex].data.forEach((data: any) => {
            data.bandNumber = bandsData[bandIndex].bandNumber;
          });
        }

        bandIndex++;
      }
    }

    bandDataControl.patchValue(bandsData);
  }

  mapBandData(bandData: AuctionOfferDerBandDataDTO[], hours: string[], auction: AuctionDayAheadDTO): IDerBandDTO[] {
    let data: IDerBandDTO[] = [];
    const auctionType = auction?.type;

    bandData.forEach((band: AuctionOfferDerBandDataDTO) => {
      const index = data.findIndex(({ bandNumber }) => bandNumber === band.bandNumber);

      if (index === -1) {
        const dataForBand = hours.map(
          (hour: string) =>
            ({
              hourNumber: hour,
              bandNumber: band.bandNumber,
              volume: null,
              acceptedVolume: null,
              price: null,
            } as AuctionOfferDerBandDataDTO)
        );

        const dataIndex = dataForBand.findIndex(({ hourNumber }) => hourNumber === band.hourNumber);

        if (dataIndex !== -1) {
          dataForBand[dataIndex] = band;
        }

        data.push({
          bandNumber: band.bandNumber,
          data: dataForBand,
        });
      } else {
        const dataIndex = data[index].data.findIndex(({ hourNumber }) => hourNumber === band.hourNumber);

        if (dataIndex !== -1) {
          data[index].data[dataIndex] = band;
        }
      }
    });
    data = data.map(value => {
      return {
        ...value,
        data: value.data.sort((a, b) => {
          const aHourIndex = hours.findIndex((hour: string) => hour === a.hourNumber);
          const bHourIndex = hours.findIndex((hour: string) => hour === b.hourNumber);

          return aHourIndex - bHourIndex;
        }),
      };
    });

    this.addMissingBands(data, auction.product?.direction);

    if (auctionType === AuctionDayAheadType.ENERGY) {
      const bandNumbers = data.map(item => item.bandNumber);
      const maxBandNumber = Math.max(...bandNumbers);
      const addBand = maxBandNumber > 0 ? -1 : 1;

      if (!bandNumbers.includes(addBand)) {
        data.push({
          bandNumber: addBand,
          data: data[0].data.map(a => {
            return { ...a, price: null, acceptedPrice: null, volume: null, acceptedVolume: null };
          }),
        });
      }
    }

    return data.sort((a, b) => b.bandNumber - a.bandNumber);
  }

  addMissingBands(data: IDerBandDTO[], auctionDirection: ProductDirection): IDerBandDTO[] {
    const missingData: IDerBandDTO[] = [];
    data
      .sort((a, b) => a.bandNumber - b.bandNumber)
      .forEach((item, index): IDerBandDTO => {
        if (data.length === 1 && data[0].bandNumber === 0) {
          switch (auctionDirection) {
            case ProductDirection.UP:
              missingData.push(this.createEmptyBandData(item, 1));
              break;
            case ProductDirection.DOWN:
              missingData.push(this.createEmptyBandData(item, -1));
              break;
            default:
              missingData.push(this.createEmptyBandData(item, -1));
              missingData.push(this.createEmptyBandData(item, 1));
              break;
          }
        }
        if (data[index + 1]) {
          let bandNumber = item.bandNumber;
          while (bandNumber + 1 < data[index + 1].bandNumber) {
            missingData.push(this.createEmptyBandData(item, bandNumber + 1));
            bandNumber += 1;
          }
        }
        return item;
      });
    data.push(...missingData);
    return data;
  }

  createEmptyBandData(item: IDerBandDTO, bandNumber: number): IDerBandDTO {
    return {
      bandNumber,
      data: item?.data.map(a => {
        return { ...a, price: null, acceptedPrice: null, volume: null, acceptedVolume: null };
      }),
    };
  }
}
