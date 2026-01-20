package com.foodcom.firstpro.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 트랜잭션의 성격(읽기 전용 vs 쓰기 가능)에 따라
 * 사용할 DataSource Key(MASTER or SLAVE)를 결정하는 라우팅 로직
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // 현재 트랜잭션이 @Transactional(readOnly = true) 인지 확인
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        return isReadOnly ? "SLAVE" : "MASTER";
    }
}
