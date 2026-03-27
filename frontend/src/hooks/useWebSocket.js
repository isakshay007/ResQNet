import { useEffect, useRef, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import { useAuth } from "../context/useAuth";

const WS_URL = import.meta.env.VITE_WS_URL || "ws://localhost:8080/ws";
const INITIAL_RECONNECT_DELAY = 1000;
const MAX_RECONNECT_DELAY = 30000;

export function useWebSocket(onNotification) {
  const { user, token } = useAuth();
  const clientRef = useRef(null);
  const callbackRef = useRef(onNotification);
  const reconnectDelayRef = useRef(INITIAL_RECONNECT_DELAY);

  useEffect(() => {
    callbackRef.current = onNotification;
  }, [onNotification]);

  useEffect(() => {
    if (!user?.email || !token) return;

    const client = new Client({
      brokerURL: WS_URL,
      reconnectDelay: reconnectDelayRef.current,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
      reconnectDelayRef.current = INITIAL_RECONNECT_DELAY;
      client.reconnectDelay = INITIAL_RECONNECT_DELAY;

      client.subscribe(
        `/queue/notifications/${user.email}`,
        (message) => {
          try {
            const notification = JSON.parse(message.body);
            callbackRef.current?.(notification);
          } catch (e) {
            console.error("Failed to parse WebSocket message:", e);
          }
        }
      );

      if (user.role === "ADMIN") {
        client.subscribe("/topic/notifications/admin", (message) => {
          try {
            const notification = JSON.parse(message.body);
            callbackRef.current?.(notification);
          } catch (e) {
            console.error("Failed to parse WebSocket message:", e);
          }
        });
      }
    };

    client.onWebSocketClose = () => {
      reconnectDelayRef.current = Math.min(
        reconnectDelayRef.current * 2,
        MAX_RECONNECT_DELAY
      );
      client.reconnectDelay = reconnectDelayRef.current;
    };

    client.onStompError = (frame) => {
      console.error("WebSocket STOMP error:", frame.headers?.["message"]);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (client.active) {
        client.deactivate();
      }
    };
  }, [user?.email, user?.role, token]);

  const disconnect = useCallback(() => {
    if (clientRef.current?.active) {
      clientRef.current.deactivate();
      clientRef.current = null;
    }
  }, []);

  return { disconnect };
}
