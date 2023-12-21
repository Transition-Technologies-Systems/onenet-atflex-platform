import * as SockJS from 'sockjs-client';

import { AuthService } from './core';
import { InjectableRxStompConfig } from '@stomp/ng2-stompjs';
import { environment as env } from '@env/environment';

export class RxStompConfig extends InjectableRxStompConfig {
  constructor(private authService: AuthService) {
    super();

    const authState = this.authService.getToken();

    this.brokerURL = `ws://${env.USER_API_URL}`;
    this.connectHeaders = {
      Authorization: authState ? `Bearer ${authState.jwt}` : '',
    };

    this.webSocketFactory = () => {
      return new SockJS('/broadcast');
    };

    this.heartbeatIncoming = 0;
    this.heartbeatOutgoing = 20000;
    this.reconnectDelay = 5000;
  }
}
