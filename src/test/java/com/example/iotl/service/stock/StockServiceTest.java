package com.example.iotl.service.stock;

import com.example.iotl.dto.stocks.StaticStockMetaDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.Stocks;
import com.example.iotl.repository.StockDetailRepository;
import com.example.iotl.repository.StocksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Mock
    private StockDetailRepository stockDetailRepository;

    @Mock
    private StocksRepository stocksRepository;

    @InjectMocks
    private StockService stockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findStocksByCode() {
        String code = "005930";
        List<StockDetail> mockList = List.of(new StockDetail());
        when(stockDetailRepository.findByStockCode(code)).thenReturn(mockList);

        List<StockDetail> result = stockService.findStocksByCode(code);

        assertEquals(1, result.size());
        verify(stockDetailRepository).findByStockCode(code);
    }

    @Test
    void findStockByStockCode() {
        String code = "005930";
        Stocks mockStock = new Stocks();
        when(stocksRepository.findStockByStockCode(code)).thenReturn(mockStock);

        Stocks result = stockService.findStockByStockCode(code);

        assertNotNull(result);
        verify(stocksRepository).findStockByStockCode(code);
    }

    @Test
    void findLatestStockByCode() {
        String code = "005930";
        StockDetail mockDetail = new StockDetail();
        when(stockDetailRepository.findTop1ByStockCodeOrderByCreatedAtDesc(code)).thenReturn(mockDetail);

        StockDetail result = stockService.findLatestStockByCode(code);

        assertNotNull(result);
        verify(stockDetailRepository).findTop1ByStockCodeOrderByCreatedAtDesc(code);
    }

    @Test
    void findAllStocks() {
        List<StockDetail> mockList = List.of(new StockDetail(), new StockDetail());
        when(stockDetailRepository.findAll()).thenReturn(mockList);

        List<StockDetail> result = stockService.findAllStocks();

        assertEquals(2, result.size());
        verify(stockDetailRepository).findAll();
    }

    @Test
    void getAllStockMetas() {
        List<Stocks> mockList = List.of(new Stocks());
        when(stocksRepository.findAll()).thenReturn(mockList);

        List<StaticStockMetaDto> result = stockService.getAllStockMetas();

        assertEquals(1, result.size());
        verify(stocksRepository).findAll();
    }

    @Test
    void findLatestStocksByCodes() {
        List<String> codes = List.of("005930", "000660");

        when(stockDetailRepository.findTop1ByStockCodeOrderByCreatedAtDesc("005930")).thenReturn(new StockDetail());
        when(stockDetailRepository.findTop1ByStockCodeOrderByCreatedAtDesc("000660")).thenReturn(new StockDetail());

        List<StockDetail> result = stockService.findLatestStocksByCodes(codes);

        assertEquals(2, result.size());
        verify(stockDetailRepository, times(2)).findTop1ByStockCodeOrderByCreatedAtDesc(anyString());
    }
}