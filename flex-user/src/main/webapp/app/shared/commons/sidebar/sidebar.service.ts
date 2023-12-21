import { Injectable } from '@angular/core';
import { Role } from '@app/shared/enums';
import { Sidebar } from './sidebar';

@Injectable()
export class SidebarService {
  public sidebar: Sidebar[] = [
    {
      name: 'sidebar.auctions.title',
      icon: 'auctions',
      anyPermission: 'FLEX_USER_AUCTIONS_OFFER_VIEW,FLEX_USER_AUCTIONS_CMVC_VIEW,FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW',
      children: [
        {
          name: 'sidebar.auctions.dayAhead',
          routerLink: '/auctions/day-ahead',
          permission: 'FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW',
        },
        {
          name: 'sidebar.auctions.cmVc',
          routerLink: '/auctions/cm-vc',
          permission: 'FLEX_USER_AUCTIONS_CMVC_VIEW',
        },
      ],
    },
    {
      name: 'sidebar.activationsSettlements',
      icon: 'activations-settlements',
      routerLink: '/activations-settlements',
      permission: 'FLEX_USER_SETTLEMENT_VIEW',
    },
    {
      name: 'sidebar.products',
      icon: 'products',
      routerLink: '/products',
      permission: 'FLEX_USER_PRODUCT_VIEW',
    },
    {
      name: 'sidebar.units',
      icon: 'ders',
      routerLink: '/ders',
      permission: 'FLEX_USER_UNIT_VIEW',
    },
    {
      name: 'sidebar.subportfolios',
      icon: 'subportfolios',
      routerLink: '/subportfolios',
      permission: 'FLEX_USER_SUBPORTFOLIO_VIEW',
    },
    {
      name: 'sidebar.flexibilityPotentials',
      icon: 'flexibility-potentials',
      routerLink: '/flexibility-potentials',
      permission: 'FLEX_USER_FP_VIEW',
      notInclude: '/register',
    },
    {
      name: 'sidebar.flexibilityPotentialsRegister',
      icon: 'flex-register',
      routerLink: '/flexibility-potentials/register',
      permission: 'FLEX_USER_FP_VIEW',
      role: [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
    },
    {
      name: 'sidebar.schedulingUnits',
      icon: 'scheduling-units',
      routerLink: '/scheduling-units',
      permission: 'FLEX_USER_SCHEDULING_UNIT_VIEW',
      notInclude: '/register',
    },
    {
      name: 'sidebar.schedulingUnitsRegister',
      icon: 'flex-register',
      routerLink: '/scheduling-units/register',
      permission: 'FLEX_USER_SCHEDULING_UNIT_VIEW',
      role: [Role.ROLE_BALANCING_SERVICE_PROVIDER],
    },
    {
      name: 'sidebar.partnership',
      icon: 'partnerships',
      routerLink: '/partnership',
      permission: 'FLEX_USER_SCHEDULING_UNIT_PROPOSAL_VIEW',
    },
    {
      name: 'sidebar.bsps',
      icon: 'bsp',
      routerLink: 'bsps',
      permission: 'FLEX_USER_BSP_VIEW',
    },
    {
      name: 'sidebar.registration',
      icon: 'registration',
      routerLink: '/registration-thread',
      permission: 'FLEX_USER_FSP_REGISTRATION_VIEW',
    },
    {
      name: 'sidebar.chat',
      icon: 'chat',
      routerLink: '/chat',
      permission: 'FLEX_USER_CHAT_VIEW',
      showCount: true,
    },
  ];
}
