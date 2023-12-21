import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Injectable } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DownloadService } from '@app/core';
import { ContentType, DerType, LocalizationType } from '@app/shared/enums';
import { Dictionary, FileDTO, FspDTO } from '@app/shared/models';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of, tap } from 'rxjs';
import { LocalizationTypeDTO } from '../dictionaries/dictionaries';
import { SchedulingUnitProposalDTO, SchedulingUnitProposalStatus } from './invite-der';
import { UnitsParameters } from './tabs/list';
import { DerTypeDTO, FspUserDTO, UnitDTO } from './unit';

import { UnitsService } from './units.service';

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }

  public instant<T>(key: T): T {
    return key;
  }
}

describe('UnitsService', () => {
  let service: UnitsService;
  let httpController: HttpTestingController;
  let translate: TranslateService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UnitsService, DownloadService, { provide: TranslateService, useClass: TranslateServiceStub }],
    });
    service = TestBed.inject(UnitsService);
    httpController = TestBed.inject(HttpTestingController);
    translate = TestBed.inject(TranslateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return canDerBeAddedToBspSchedulingUnit', (done: DoneFn) => {
    const mockDerId = 1;
    const mockBspId = 2;
    service
      .canDerBeAddedToBspSchedulingUnit(mockDerId, mockBspId)
      .pipe(
        tap(response => {
          expect(response).toEqual(true);
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne(
      `/services/flex-server/api/admin/scheduling-units/proposal/can-der-be-added-to-bsp-scheduling-unit?derId=${mockDerId}`
    );
    expect(req.request.method).toEqual('GET');
    req.flush(true);
    httpController.verify();
  });

  it('should return Unit', (done: DoneFn) => {
    const mockUnitDTO: UnitDTO = {
      id: 1,
    } as UnitDTO;

    service
      .getUnit(1)
      .pipe(
        tap(response => {
          expect(response).toEqual(mockUnitDTO);
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne('/services/flex-server/api/admin/units/1');
    expect(req.request.method).toEqual('GET');
    req.flush(mockUnitDTO);
    httpController.verify();
  });

  it('should return getCompanies', (done: DoneFn) => {
    const mockData: any[] = [
      { id: 1, companyName: 'name1' },
      { id: 2, companyName: 'name2' },
    ];
    const mockCompanies: Dictionary[] = [
      { id: 1, value: 1, label: 'name1' },
      { id: 2, value: 2, label: 'name2' },
    ];

    service
      .getCompanies()
      .pipe(
        tap(response => {
          expect(response).toEqual(mockCompanies);
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne(
      '/services/flex-server/api/fsps/get-company?roles=ROLE_FLEX_SERVICE_PROVIDER,ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED'
    );
    expect(req.request.method).toEqual('GET');
    req.flush(mockData);
    httpController.verify();
  });

  it('should return getDerType', () => {
    spyOn(translate, 'instant')
      .withArgs('DER_TYPE.NUCLEAR')
      .and.returnValue('Nuclear')
      .withArgs('DerType.ENERGY_STORAGE')
      .and.returnValue('Energy storage')
      .withArgs('DerType.GENERATION')
      .and.returnValue('Generation')
      .withArgs('DER_TYPE.NOWYTYPDERA')
      .and.returnValue('nowyTypDera')
      .withArgs('DerType.RECEPTION')
      .and.returnValue('Reception')
      .withArgs('DER_TYPE.RECEPTIONTYPE')
      .and.returnValue('receptionType');
    const mockUnitDto: UnitDTO = {
      derTypeReception: {
        id: 227,
        type: DerType.RECEPTION,
        nlsCode: 'DER_TYPE.RECEPTIONTYPE',
        value: '',
      },
      derTypeEnergyStorage: {
        id: 188,
        type: DerType.ENERGY_STORAGE,
        value: '',
        nlsCode: 'DER_TYPE.NUCLEAR',
      },
      derTypeGeneration: {
        id: 407,
        type: DerType.GENERATION,
        value: '',
        nlsCode: 'DER_TYPE.NOWYTYPDERA',
      },
    } as UnitDTO;
    const expectedValue = 'Energy storage: Nuclear/ Generation: nowyTypDera/ Reception: receptionType';
    const response = service.getDerType(mockUnitDto);
    expect(response).toEqual(expectedValue);
  });

  it('should return DerTypesDict', (done: DoneFn) => {
    const mockDerTypesDict: DerTypeDTO[] = [
      { id: 1, value: 'A', nlsCode: 'A' },
      { id: 2, value: 'B', nlsCode: 'B' },
    ];

    service
      .getDerTypesDict()
      .pipe(
        tap(response => {
          expect(response).toEqual(mockDerTypesDict);
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne('/services/flex-server/api/dictionary/get-by-type/DER_TYPE');
    expect(req.request.method).toEqual('GET');
    req.flush(mockDerTypesDict);
    httpController.verify();
  });

  it('should fetch getDerTypesWithType', (done: DoneFn) => {
    const derTypes: Array<DerTypeDTO & { type: DerType }> = [
      { id: 1, value: '', nlsCode: '', type: DerType.ENERGY_STORAGE },
      { id: 2, value: '', nlsCode: '', type: DerType.GENERATION },
    ];

    service
      .getDerTypesWithType()
      .pipe(
        tap(response => {
          expect(response).toEqual(derTypes);
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne('/services/flex-server/api/dictionary/get-der-types');
    expect(req.request.method).toBe('GET');
    req.flush(derTypes);
    httpController.verify();
  });

  it('should fetch getLocalizationsDict', (done: DoneFn) => {
    const localizationTypes: LocalizationTypeDTO[] = [
      { id: 1, name: '', type: LocalizationType.COUPLING_POINT_ID },
      { id: 2, name: '', type: LocalizationType.POWER_STATION_ML_LV_NUMBER },
      { id: 3, name: '', type: LocalizationType.POINT_OF_CONNECTION_WITH_LV },
    ];

    const mockParams = [
      LocalizationType.COUPLING_POINT_ID,
      LocalizationType.POWER_STATION_ML_LV_NUMBER,
      LocalizationType.POINT_OF_CONNECTION_WITH_LV,
    ].join(',');

    service
      .getLocalizationsDict()
      .pipe(
        tap(response => {
          expect(response).toEqual(localizationTypes);
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne(
      '/services/flex-server/api/admin/localization-types/get-by-type?types=COUPLING_POINT_ID,POWER_STATION_ML_LV_NUMBER,POINT_OF_CONNECTION_WITH_LV'
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('types')).toEqual(mockParams);
    req.flush(localizationTypes);
    httpController.verify();
  });

  it('should return getUnits', (done: DoneFn) => {
    const mockData: { id: number; name: string; sder: boolean }[] = [
      { id: 1, name: 'test', sder: true },
      { id: 2, name: 'test2', sder: false },
    ];
    const mockResponse: any[] = [
      {
        id: 1,
        name: 'test',
        sder: true,
        value: 1,
        label: `test(SDER)`,
      },
      {
        id: 2,
        name: 'test2',
        sder: false,
        value: 2,
        label: `test2`,
      },
    ];

    service
      .getUnits()
      .pipe(
        tap(response => {
          expect(response).toEqual(mockResponse);
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne('/services/flex-server/api/admin/units/get-all?certified.equals=true');
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
    httpController.verify();
  });

  describe('should export XLSX: ', () => {
    it('with displayed data', () => {
      const downloadSpy = spyOn(DownloadService, 'saveFileWithParam').and.callThrough();
      const parameters: UnitsParameters = {
        filters: { name: 'Test' },
        sort: 'name',
        page: 0,
        size: 25,
      };
      const file: FileDTO = {
        fileName: 'test.xlsx',
        fileExtension: 'xlsx',
        base64StringData: 'dGVzdA==',
      };

      service.exportXLSX(parameters, false);

      const req = httpController.expectOne(
        '/services/flex-server/api/admin/units/export/displayed-data?name=Test&sort=name&page=0&size=25'
      );
      expect(req.request.method).toBe('GET');
      req.flush(file);

      expect(downloadSpy).toHaveBeenCalledWith('dGVzdA==', 'test.xlsx', ContentType.XLSX, true);
      httpController.verify();
    });

    it('with all data', () => {
      const downloadSpy = spyOn(DownloadService, 'saveFileWithParam').and.callThrough();
      const file: FileDTO = {
        fileName: 'test.xlsx',
        fileExtension: 'xlsx',
        base64StringData: 'dGVzdA==',
      };

      service.exportXLSX(undefined, true);

      const req = httpController.expectOne('/services/flex-server/api/admin/units/export/all');
      expect(req.request.method).toBe('GET');
      req.flush(file);

      expect(downloadSpy).toHaveBeenCalledWith('dGVzdA==', 'test.xlsx', ContentType.XLSX, true);
      httpController.verify();
    });
  });

  it('should post saveProposal', (done: DoneFn) => {
    const mockProposal: Partial<SchedulingUnitProposalDTO> = {
      id: 1,
      status: SchedulingUnitProposalStatus.NEW,
      schedulingUnitId: 2,
      unitId: 3,
      senderId: 4,
      details: {
        fspName: '',
        derName: '',
        derType: '',
        derSourcePower: 2,
        derConnectionPower: 2,
      },

      createdBy: new Date().toString(),
      createdDate: new Date().toString(),
      lastModifiedBy: new Date().toString(),
      lastModifiedDate: new Date().toString(),
    };

    service
      .saveProposal(mockProposal)
      .pipe(
        tap(response => {
          expect(JSON.stringify(response)).toEqual(JSON.stringify(mockProposal));
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne(`/services/flex-server/api/admin/scheduling-units/proposal/invite-der`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockProposal);
    req.flush(mockProposal);
    httpController.verify();
  });

  it('should update Unit', (done: DoneFn) => {
    // @ts-ignore
    spyOn(service, 'formatDateTime').and.callThrough();
    const mockUnit: UnitDTO = {
      id: 1,
      name: 'Test unit',
      code: 'TU1',
      location: 'Test location',
      sourcePower: 100,
      connectionPower: 50,
      directionOfDeviation: 'UP',
      derType: { id: 1, value: 'Test der type', nlsCode: '1234' },
      aggregated: false,
      fspId: 2,
      validFrom: '2022-01-01T00:00:00',
      validTo: '2022-12-31T00:00:00',
      active: true,
      certified: true,
      fsp: {} as FspDTO,
      fspUser: {} as FspUserDTO,
      geoLocations: [],
      schedulingUnit: undefined,
      subportfolio: undefined,
      createdBy: 'Test user',
      createdDate: '2022-01-01T00:00:00',
      lastModifiedBy: 'Test user',
      lastModifiedDate: '2022-01-01T00:00:00',
      ppe: '',
      mridDso: '',
      mridTso: '',
      sder: false,
      version: 1,
      balancedByFlexPotentialProduct: false,
      couplingPointIdTypes: [],
      powerStationTypes: [],
      pointOfConnectionWithLvTypes: [],
      pmin: 0,
      qmin: 0,
      qmax: 0,
      derTypeReception: undefined,
      derTypeEnergyStorage: undefined,
      derTypeGeneration: undefined,
    };

    service
      .update(1, mockUnit)
      .pipe(
        tap(response => {
          expect(JSON.stringify(response)).toEqual(JSON.stringify(mockUnit));
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne('/services/flex-server/api/admin/units');
    expect(req.request.method).toBe('PUT');
    // @ts-ignore
    expect(service.formatDateTime).toHaveBeenCalledWith(mockUnit, ['validFrom', 'validTo']);
    req.flush(mockUnit);
    httpController.verify();
  });

  it('should remove Unit', (done: DoneFn) => {
    service
      .remove(1)
      .pipe(
        tap(response => {
          expect(response).toBeNull();
          done();
        })
      )
      .subscribe();

    const req = httpController.expectOne('/services/flex-server/api/admin/units/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    httpController.verify();
  });
});
