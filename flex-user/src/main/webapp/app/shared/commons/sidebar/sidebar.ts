import { Role } from '@app/shared/enums';

export interface SidebarChildren {
  name: string;
  active?: boolean;
  routerLink: string;
  permission?: string;
}

export interface Sidebar {
  name: string;
  icon: string;
  role?: Role[];
  active?: boolean;
  opened?: boolean;
  routerLink?: string;
  permission?: string;
  notInclude?: string;
  anyPermission?: string;
  children?: Array<SidebarChildren>;
  showCount?: boolean;
}
