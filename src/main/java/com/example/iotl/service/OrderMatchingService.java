package com.example.iotl.service;


import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.TradeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderMatchingService {
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TradeService tradeService;


    //신규 주문이 들어오면,
    //대기 중인 반대 주문과 자동 매칭을 시도하고,
    //체결 가능한 경우 TradeService를 통해 체결까지 연결시키기

    public void match(Order newOrder) {
            // 1. 반대 타입 주문 찾기
            OrderType oppositeType = newOrder.getOrderType() == OrderType.BUY
                ? OrderType.SELL
                : OrderType.BUY;

            // 2. 대기 상태이면서, 같은 종목에 대한 반대 주문 리스트 가져오기
            List<Order> candidateOrders = orderRepository
                .findMatchingOrders(oppositeType, newOrder.getStock().getStockCode(), newOrder.getPrice());

            // 3. 매칭 조건이 되는 주문과 체결 시도
            for (Order candidate : candidateOrders) {
                // 가격 비교 조건 등 추가 가능
                //newOrder는 사용자가 방금 등록한 주문이고,
                // candidate는 반대 타입(예: 매수 시 매도)의 기존 주문 중 매칭된 대상
                tradeService.trade(newOrder, candidate);
                break;  // 단순 구현: 한 건만 체결 후 종료
            }
        }


    }



