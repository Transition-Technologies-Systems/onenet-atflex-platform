import { Injectable } from '@angular/core';
import { Sidebar } from './sidebar';

@Injectable()
export class SidebarService {
  public sidebar: Sidebar[] = [
    {
      name: 'sidebar.auctions.title',
      icon: 'auctions',
      anyPermission:
        'FLEX_ADMIN_ALGORITHM_EVALUATIONS,FLEX_ADMIN_AUCTIONS_OFFER_VIEW,FLEX_ADMIN_AUCTIONS_CMVC_VIEW,FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW',
      children: [
        {
          name: 'sidebar.auctions.algorithmEvaluations',
          routerLink: '/auctions/algorithm-evaluations',
          permission: 'FLEX_ADMIN_ALGORITHM_EVALUATIONS',
        },
        {
          name: 'sidebar.auctions.bidsEvaluation',
          routerLink: '/auctions/bids-evaluation',
          permission: 'FLEX_ADMIN_AUCTIONS_OFFER_VIEW',
        },
        {
          name: 'sidebar.auctions.dayAhead',
          routerLink: '/auctions/day-ahead',
          permission: 'FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW',
        },
        {
          name: 'sidebar.auctions.cmVc',
          routerLink: '/auctions/cm-vc',
          permission: 'FLEX_ADMIN_AUCTIONS_CMVC_VIEW',
        },
      ],
    },
    {
      name: 'sidebar.activationsSettlements',
      icon: 'activations-settlements',
      routerLink: '/activations-settlements',
      permission: 'FLEX_ADMIN_SETTLEMENT_VIEW',
    },
    {
      name: 'sidebar.products',
      icon: 'products',
      routerLink: '/products',
      permission: 'FLEX_ADMIN_PRODUCT_VIEW',
    },
    {
      name: 'sidebar.units',
      icon: 'ders',
      routerLink: '/ders',
      permission: 'FLEX_ADMIN_UNIT_VIEW',
    },
    {
      name: 'sidebar.subportfolios',
      icon: 'subportfolios',
      routerLink: '/subportfolios',
      permission: 'FLEX_ADMIN_SUBPORTFOLIO_VIEW',
    },
    {
      name: 'sidebar.flexibilityPotentials',
      icon: 'flexibility-potentials',
      routerLink: '/flexibility-potentials',
      permission: 'FLEX_ADMIN_FP_VIEW',
      notInclude: '/register',
    },
    {
      name: 'sidebar.schedulingUnits',
      icon: 'scheduling-units',
      routerLink: '/scheduling-units',
      permission: 'FLEX_ADMIN_SCHEDULING_UNIT_VIEW',
      notInclude: '/register',
    },
    {
      name: 'sidebar.partnership',
      icon: 'partnerships',
      routerLink: '/partnership',
      permission: 'FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_VIEW',
    },
    {
      name: 'sidebar.flexRegister',
      icon: 'flex-register',
      anyPermission: 'FLEX_ADMIN_FP_VIEW,FLEX_ADMIN_SCHEDULING_UNIT_VIEW',
      children: [
        {
          name: 'sidebar.flexibilityPotentialsRegister',
          routerLink: '/flexibility-potentials/register',
          permission: 'FLEX_ADMIN_FP_VIEW',
        },
        {
          name: 'sidebar.schedulingUnitsRegister',
          routerLink: '/scheduling-units/register',
          permission: 'FLEX_ADMIN_SCHEDULING_UNIT_VIEW',
        },
      ],
    },
    {
      name: 'sidebar.registration',
      icon: 'registration',
      routerLink: '/registration',
      permission: 'FLEX_ADMIN_FSP_REGISTRATION_VIEW',
    },
    {
      name: 'sidebar.users',
      icon: 'users',
      routerLink: 'users',
      permission: 'FLEX_ADMIN_USER_VIEW',
    },
    {
      name: 'sidebar.fsps',
      icon: 'fsp',
      routerLink: 'fsps',
      permission: 'FLEX_ADMIN_FSP_VIEW',
    },
    {
      name: 'sidebar.bsps',
      icon: 'bsp',
      routerLink: 'bsps',
      permission: 'FLEX_ADMIN_FSP_VIEW',
    },
    {
      name: 'sidebar.dictionaries.title',
      icon: 'dictionaries',
      routerLink: 'dictionaries',
      anyPermission: 'FLEX_ADMIN_DER_TYPE_VIEW,FLEX_ADMIN_SCHEDULING_UNIT_TYPE_VIEW',
      children: [
        {
          name: 'sidebar.dictionaries.children.der',
          routerLink: 'dictionaries/der-type',
          permission: 'FLEX_ADMIN_DER_TYPE_VIEW',
        },
        {
          name: 'sidebar.dictionaries.children.su',
          routerLink: 'dictionaries/su-type',
          permission: 'FLEX_ADMIN_SCHEDULING_UNIT_TYPE_VIEW',
        },
        {
          name: 'sidebar.dictionaries.children.localization',
          routerLink: 'dictionaries/localization-type',
          permission: 'FLEX_ADMIN_LOCALIZATION_TYPE_VIEW',
        },
        {
          name: 'sidebar.dictionaries.children.kdmModels',
          routerLink: 'dictionaries/kdm-models',
          permission: 'FLEX_ADMIN_KDM_MODEL_VIEW',
        },
      ],
    },
    {
      name: 'sidebar.chat',
      icon: 'chat',
      routerLink: '/chat',
      permission: 'FLEX_ADMIN_CHAT_VIEW',
      showCount: true,
    },
    {
      name: 'sidebar.oneNetSystem.title',
      icon: 'onenet-system',
      routerLink: 'onenet-system',
      anyPermission: 'FLEX_ADMIN_ONENET_USER_VIEW',
      children: [
        {
          name: 'sidebar.oneNetSystem.children.onsUsers',
          routerLink: 'onenet-system/ons-users',
          permission: 'FLEX_ADMIN_ONENET_USER_VIEW',
        },
        {
          name: 'sidebar.oneNetSystem.children.consumeData',
          routerLink: 'onenet-system/consume-data',
          permission: 'FLEX_ADMIN_CONSUME_DATA_VIEW',
        },
        {
          name: 'sidebar.oneNetSystem.children.offeredServices',
          routerLink: 'onenet-system/offered-services',
          permission: 'FLEX_ADMIN_OFFERED_SERVICES_VIEW',
        },
        {
          name: 'sidebar.oneNetSystem.children.provideData',
          routerLink: 'onenet-system/provide-data',
          permission: 'FLEX_ADMIN_PROVIDE_DATA_VIEW',
        },
      ],
    },
    {
      name: 'sidebar.kpi',
      icon: 'kpi',
      routerLink: 'kpi',
      permission: 'FLEX_ADMIN_KPI_VIEW',
    },
  ];
}
