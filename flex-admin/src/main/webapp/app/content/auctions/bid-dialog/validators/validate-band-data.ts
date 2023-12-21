import { AbstractControl, FormArray, FormGroup, ValidationErrors } from '@angular/forms';
import { AuctionOfferDerBandDataDTO, AuctionOfferDerBandDataForm } from '../../offers/offer';

import { isEqual } from 'lodash-es';
import { AuctionDayAheadType } from '../../enums';

function clearErrors(control: AbstractControl | undefined | null): void {
  if (!control) {
    return;
  }

  const currentErrors = control.errors ?? {};
  const { required, bandHourNoFilled, timestampNoFilled, limitExceed, ...errors } = currentErrors;

  if (isEqual(errors, currentErrors)) {
    return;
  }

  control.setErrors(Object.keys(errors).length ? errors : null);
}

function controlBandHourNoFilledError(
  control: AbstractControl | null | undefined,
  addError: boolean,
  isClear: boolean = false,
  auctionType: AuctionDayAheadType = AuctionDayAheadType.ENERGY
) {
  if (
    control &&
    (addError ||
      Object.keys(control.errors ?? {}).includes('bandHourNoFilled') ||
      Object.keys(control.errors ?? {}).includes('timestampNoFilled'))
  ) {
    let { bandHourNoFilled, timestampNoFilled, required, ...errors } = control.errors || {};

    if (addError) {
      if (auctionType === AuctionDayAheadType.CAPACITY) {
        errors = {
          timestampNoFilled: true,
        };
      } else {
        errors = {
          bandHourNoFilled: true,
        };
      }
    }

    control.setErrors(Object.keys(errors).length ? errors : null);

    if (addError || isClear) {
      control.parent?.get('price')?.setErrors(null);
    }
  }
}

function getFilledBandsForHour(
  bands: AuctionOfferDerBandDataForm[],
  control: AbstractControl,
  controlKey: string,
  band: number,
  hour: string
): AuctionOfferDerBandDataDTO[] {
  const valuesForHour = bands.flatMap(({ data }) => data?.filter(({ hourNumber }) => hourNumber === hour) ?? []);
  const isAccepted = ['acceptedPrice', 'acceptedVolume'].includes(controlKey);

  return valuesForHour.filter(({ volume, price, acceptedVolume, acceptedPrice, bandNumber }) => {
    let volumeValue = isAccepted ? acceptedVolume : volume;
    let priceValue = isAccepted ? acceptedPrice : price;

    if (bandNumber === band) {
      if (['acceptedPrice', 'price'].includes(controlKey)) {
        priceValue = control.value;
      }

      if (['acceptedVolume', 'volume'].includes(controlKey)) {
        volumeValue = control.value;
      }
    }

    if ((volumeValue !== null && volumeValue !== undefined) || (priceValue !== null && priceValue !== undefined)) {
      return true;
    }

    return false;
  });
}

function getFilledBand(filled: AuctionOfferDerBandDataDTO[]): {
  minusBand: AuctionOfferDerBandDataDTO | undefined;
  plusBand: AuctionOfferDerBandDataDTO | undefined;
  bandsToCheck: number[];
} {
  let filledMinusData: AuctionOfferDerBandDataDTO | undefined;
  let filledPlusData: AuctionOfferDerBandDataDTO | undefined;
  let bandsToCheck: number[] = [];

  if (filled.length) {
    const [bandPlusData] = filled
      .filter(({ bandNumber }) => bandNumber > 0)
      .sort((a, b) => Math.abs(b.bandNumber) - Math.abs(a.bandNumber));

    const [bandMinusData] = filled
      .filter(({ bandNumber }) => bandNumber < 0)
      .sort((a, b) => Math.abs(b.bandNumber) - Math.abs(a.bandNumber));

    filledPlusData = bandPlusData;
    filledMinusData = bandMinusData;
  }

  if (filled.length) {
    if (filledPlusData) {
      const filledBand = filledPlusData.bandNumber;
      bandsToCheck = Array.from({ length: Math.abs(filledBand) }, (_, i: number) => filledBand - i);
    }

    if (filledMinusData) {
      const filledBand = filledMinusData.bandNumber;

      const minusBandsToCheck = Array.from({ length: Math.abs(filledBand) }, (_, i: number) => filledBand + i);
      bandsToCheck = [...bandsToCheck, ...minusBandsToCheck];
    }
  }

  return {
    minusBand: filledMinusData,
    plusBand: filledPlusData,
    bandsToCheck,
  };
}

function someAcceptedBandFilled(
  data: AuctionOfferDerBandDataForm[],
  band: number,
  hour: string,
  controlKey: string,
  controlValue: any
): boolean {
  return data
    .filter(({ bandNumber }) => bandNumber !== 0)
    .some(({ data, bandNumber }) =>
      data?.some(({ acceptedVolume, acceptedPrice, hourNumber }) => {
        let volumeValue = acceptedVolume;
        let priceValue = acceptedPrice;

        if (bandNumber === band && hourNumber === hour) {
          if (controlKey === 'acceptedPrice') {
            priceValue = controlValue;
          }

          if (controlKey === 'acceptedVolume') {
            volumeValue = controlValue;
          }
        }

        return priceValue !== null || volumeValue !== null;
      })
    );
}

function someBandFilled(data: AuctionOfferDerBandDataForm[], band: number, hour: string, controlKey: string, controlValue: any): boolean {
  return data
    .filter(({ bandNumber }) => bandNumber !== 0)
    .some(({ data, bandNumber }) => {
      if (!data) {
        return null;
      }
      return data.some(({ volume, price, hourNumber }) => {
        let volumeValue = volume;
        let priceValue = price;

        if (bandNumber === band && hourNumber === hour) {
          if (controlKey === 'price') {
            priceValue = controlValue;
          }

          if (controlKey === 'volume') {
            volumeValue = controlValue;
          }
        }

        return priceValue !== null || volumeValue !== null;
      });
    });
}

export function validateBandDataForCapacity(hour: string, band: number, index: number, controlKey: string) {
  return (control: AbstractControl): ValidationErrors | null => {
    const group = control.parent?.parent?.parent?.parent?.parent;
    const isAccepted = ['acceptedPrice', 'acceptedVolume'].includes(controlKey);
    const data: AuctionOfferDerBandDataForm[] = group?.get('bandData')?.value ?? [];
    const bands: AuctionOfferDerBandDataForm[] = data.filter(
      ({ bandNumber }: AuctionOfferDerBandDataForm) => bandNumber > 0 || bandNumber < 0
    );
    const volumeKey = isAccepted ? 'acceptedVolume' : 'volume';
    const priceKey = isAccepted ? 'acceptedPrice' : 'price';

    const someAcceptedFilled = isAccepted ? someAcceptedBandFilled(data, band, hour, controlKey, control.value) : false;
    const otherAcceptedFilled = isAccepted ? someAcceptedBandFilled(data, band, hour, controlKey, null) : false;
    const someFilled = someBandFilled(data, band, hour, controlKey, control.value);
    const otherFilled = someBandFilled(data, band, hour, controlKey, null);

    if (isAccepted && !someAcceptedFilled) {
      return null;
    }

    const filled = getFilledBandsForHour(bands, control, controlKey, band, hour);

    const bandMinusIndex = data.findIndex(({ bandNumber }) => bandNumber === -1);
    const bandPlusIndex = data.findIndex(({ bandNumber }) => bandNumber === 1);

    validateControlsInHour(data, group, index, volumeKey, priceKey);

    if (!filled.length && (someAcceptedFilled || !isAccepted) && (!someFilled || !otherFilled)) {
      controlBandHourNoFilledError(
        group?.get(`bandData.${bandPlusIndex}.data.${index}.${volumeKey}`),
        true,
        undefined,
        AuctionDayAheadType.CAPACITY
      );
      controlBandHourNoFilledError(
        group?.get(`bandData.${bandMinusIndex}.data.${index}.${volumeKey}`),
        true,
        undefined,
        AuctionDayAheadType.CAPACITY
      );
    } else {
      controlBandHourNoFilledError(
        group?.get(`bandData.${bandPlusIndex}.data.${index}.${volumeKey}`),
        false,
        undefined,
        AuctionDayAheadType.CAPACITY
      );
      controlBandHourNoFilledError(
        group?.get(`bandData.${bandMinusIndex}.data.${index}.${volumeKey}`),
        false,
        undefined,
        AuctionDayAheadType.CAPACITY
      );
    }

    if (someAcceptedFilled && !otherAcceptedFilled && isAccepted) {
      const bandArrayControl = control.parent?.parent as FormArray;

      if (bandArrayControl) {
        bandArrayControl.controls.forEach(bandControl => {
          const acceptedPriceControl = bandControl.get('acceptedPrice');
          const acceptedVolumeControl = bandControl.get('acceptedVolume');

          if (acceptedPriceControl && acceptedPriceControl.value === null) {
            acceptedPriceControl.setErrors({ required: true });
          }

          if (acceptedVolumeControl && acceptedVolumeControl.value === null) {
            acceptedVolumeControl.setErrors({ required: true });
          }
        });
      }
    }

    validateBandsBeetwenFilled(filled, data, isAccepted, group, index, AuctionDayAheadType.CAPACITY);

    if (control.value === 0 && controlKey === 'volume') {
      return {
        disabledValue: true,
      };
    }

    const { timestampNoFilled = null, required = null } = control.errors ?? {};
    const errors = Object.entries({ timestampNoFilled, required }).reduce((errors: any, [key, value]) => {
      if (!value) {
        return errors;
      }

      return {
        ...errors,
        [key]: value,
      };
    }, {});

    return Object.keys(errors).length ? errors : null;
  };
}

export function validateBandDataForEnergy(hour: string, band: number, index: number, controlKey: string) {
  return (control: AbstractControl): ValidationErrors | null => {
    const group = control.parent?.parent?.parent?.parent?.parent;
    const data: AuctionOfferDerBandDataForm[] = group?.get('bandData')?.value ?? [];
    const bands: AuctionOfferDerBandDataForm[] = data.filter(
      ({ bandNumber }: AuctionOfferDerBandDataForm) => bandNumber > 0 || bandNumber < 0
    );

    const isAccepted = ['acceptedPrice', 'acceptedVolume'].includes(controlKey);
    const volumeKey = isAccepted ? 'acceptedVolume' : 'volume';
    const priceKey = isAccepted ? 'acceptedPrice' : 'price';

    const someAcceptedFilled = isAccepted ? someAcceptedBandFilled(data, band, hour, controlKey, control.value) : false;
    const otherAcceptedFilled = isAccepted ? someAcceptedBandFilled(data, band, hour, controlKey, null) : false;
    const someFilled = someBandFilled(data, band, hour, controlKey, control.value);
    const otherFilled = someBandFilled(data, band, hour, controlKey, null);
    if (!bands.length) {
      return null;
    }

    const filled = getFilledBandsForHour(bands, control, controlKey, band, hour);

    const bandMinusIndex = data.findIndex(({ bandNumber }) => bandNumber === -1);
    const bandPlusIndex = data.findIndex(({ bandNumber }) => bandNumber === 1);

    validateControlsInHour(data, group, index, volumeKey, priceKey);

    if (!filled.length && (someAcceptedFilled || !isAccepted) && (!someFilled || !otherFilled)) {
      controlBandHourNoFilledError(group?.get(`bandData.${bandPlusIndex}.data.${index}.${volumeKey}`), true);
      controlBandHourNoFilledError(group?.get(`bandData.${bandMinusIndex}.data.${index}.${volumeKey}`), true);
    } else {
      controlBandHourNoFilledError(group?.get(`bandData.${bandPlusIndex}.data.${index}.${volumeKey}`), false);
      controlBandHourNoFilledError(group?.get(`bandData.${bandMinusIndex}.data.${index}.${volumeKey}`), false);
    }

    if (someAcceptedFilled && !otherAcceptedFilled && isAccepted) {
      const bandArrayControl = control.parent?.parent as FormArray;

      if (bandArrayControl) {
        bandArrayControl.controls.forEach((_, dataIndex: number) => {
          if (dataIndex === index) {
            return;
          }

          const acceptedVolumePlusControl = group?.get(`bandData.${bandPlusIndex}.data.${dataIndex}.${volumeKey}`);
          const acceptedVolumeMinusControl = group?.get(`bandData.${bandMinusIndex}.data.${dataIndex}.${volumeKey}`);

          if (
            (bandPlusIndex === -1 || (acceptedVolumePlusControl && acceptedVolumePlusControl.value === null)) &&
            (bandMinusIndex === -1 || (acceptedVolumeMinusControl && acceptedVolumeMinusControl.value === null))
          ) {
            acceptedVolumePlusControl?.setErrors({ bandHourNoFilled: true });
            acceptedVolumeMinusControl?.setErrors({ bandHourNoFilled: true });
          }
        });
      }
    }

    validateBandsBeetwenFilled(filled, data, isAccepted, group, index);

    if (control.value === 0 && controlKey === 'volume') {
      return {
        disabledValue: true,
      };
    }

    const { bandHourNoFilled = null, required = null, limitExceed = null } = control.errors ?? {};
    const errors = Object.entries({ bandHourNoFilled, required, limitExceed }).reduce((errors: any, [key, value]) => {
      if (!value) {
        return errors;
      }

      return {
        ...errors,
        [key]: value,
      };
    }, {});

    return Object.keys(errors).length ? errors : null;
  };
}

function validateControlsInHour(
  data: AuctionOfferDerBandDataForm[],
  group: FormGroup<any> | FormArray<any> | null | undefined,
  index: number,
  volumeKey: string,
  priceKey: string
): void {
  data.forEach(({ bandNumber }, dataIndex: number) => {
    const volumeControl = group?.get(`bandData.${dataIndex}.data.${index}.${volumeKey}`);
    const priceControl = group?.get(`bandData.${dataIndex}.data.${index}.${priceKey}`);

    clearErrors(volumeControl);

    if (volumeControl && priceControl) {
      if (volumeControl.value === null && priceControl.value === null) {
        clearErrors(priceControl);
      } else if (volumeControl.value !== null && priceControl.value === null) {
        if (bandNumber !== 0) {
          priceControl.setErrors({ required: true });
        }
      } else if (priceControl.value !== null && volumeControl.value === null) {
        volumeControl.setErrors({ required: true });
      }
    }
  });
}

function validateBandsBeetwenFilled(
  filled: AuctionOfferDerBandDataDTO[],
  data: AuctionOfferDerBandDataForm[],
  isAccepted: boolean,
  group: FormGroup<any> | FormArray<any> | null | undefined,
  index: number,
  auctionType: AuctionDayAheadType = AuctionDayAheadType.ENERGY
): void {
  if (filled.length) {
    const { bandsToCheck } = getFilledBand(filled);
    const plusBands = bandsToCheck.filter(item => item > 0);
    const minusBands = bandsToCheck.filter(item => item < 0);

    bandsToCheck.forEach((bandToCheck: number) => {
      const bandIndex = data.findIndex(({ bandNumber }) => bandNumber === bandToCheck);
      const keys = isAccepted ? ['acceptedVolume', 'acceptedPrice'] : ['price', 'volume'];

      keys.forEach((key: string) => {
        const bandControl = group?.get(`bandData.${bandIndex}.data.${index}.${key}`);

        if (!bandControl || bandControl?.disabled) {
          return;
        }

        if (bandControl.value === null && bandToCheck !== 0) {
          bandControl.setErrors({ required: true });
          bandControl.markAsTouched();
        } else {
          clearErrors(bandControl);
        }
      });
    });

    if (auctionType === AuctionDayAheadType.ENERGY) {
      validateLimitExceedForEnergy(data, isAccepted, group, index, plusBands);
      validateLimitExceedForEnergy(data, isAccepted, group, index, minusBands);
    }
  }
}

function validateLimitExceedForEnergy(
  data: AuctionOfferDerBandDataForm[],
  isAccepted: boolean,
  group: FormGroup<any> | FormArray<any> | null | undefined,
  index: number,
  bandsToCheck: number[]
): void {
  bandsToCheck.forEach((bandToCheck: number) => {
    const volumeKey = isAccepted ? 'acceptedVolume' : 'volume';
    const volumeValue = bandsToCheck.reduce((acc, bandNum) => {
      const bandIdx = data.findIndex(({ bandNumber }) => bandNumber === bandNum);
      const volumeControl = group?.get(`bandData.${bandIdx}.data.${index}.${volumeKey}`);
      return acc + volumeControl?.value;
    }, 0);

    const bandIndex = data.findIndex(({ bandNumber }) => bandNumber === bandToCheck);
    const keys = isAccepted ? ['acceptedVolume', 'acceptedPrice'] : ['price', 'volume'];
    const bandNumber = group?.get(`bandData.${bandIndex}.data.${index}.bandNumber`);
    const selfSchedule = group?.get(`bandData.${bandIndex}.data.${index}.selfSchedule`);
    const pmin = group?.get(`bandData.${bandIndex}.data.${index}.pmin`);
    const sourcePower = group?.get(`bandData.${bandIndex}.data.${index}.sourcePower`);

    keys.forEach((key: string) => {
      const bandControl = group?.get(`bandData.${bandIndex}.data.${index}.${key}`);

      if (!bandControl || bandControl?.disabled) {
        return;
      }

      if (key === volumeKey && bandControl.value !== null && bandToCheck !== 0) {
        const { limitExceed, ...rest } = bandControl.errors || {};
        if (bandNumber?.value > 0) {
          if (selfSchedule?.value + volumeValue <= sourcePower?.value) {
            bandControl.setErrors(Object.keys({ ...rest }).length ? { ...rest } : null);
          } else {
            bandControl.setErrors({ ...rest, limitExceed: true });
            bandControl.markAsTouched();
          }
        } else {
          if (selfSchedule?.value - volumeValue >= pmin?.value) {
            bandControl.setErrors(Object.keys({ ...rest }).length ? { ...rest } : null);
          } else {
            bandControl.setErrors({ ...rest, limitExceed: true });
            bandControl.markAsTouched();
          }
        }
      }
    });
  });
}
