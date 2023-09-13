package otus.study.cashmachine.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import otus.study.cashmachine.TestUtil;
import otus.study.cashmachine.bank.dao.CardsDao;
import otus.study.cashmachine.bank.data.Card;
import otus.study.cashmachine.bank.service.impl.CardServiceImpl;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CardServiceTest {
    AccountService accountService;

    CardsDao cardsDao;

    CardService cardService;

    @BeforeEach
    void init() {
        cardsDao = mock(CardsDao.class);
        accountService = mock(AccountService.class);
        cardService = new CardServiceImpl(accountService, cardsDao);
    }

    @Test
    void testCreateCard() {
        when(cardsDao.createCard("5555", 1L, "0123")).thenReturn(
                new Card(1L, "5555", 1L, "0123"));

        Card newCard = cardService.createCard("5555", 1L, "0123");
        assertNotEquals(0, newCard.getId());
        assertEquals("5555", newCard.getNumber());
        assertEquals(1L, newCard.getAccountId());
        assertEquals("0123", newCard.getPinCode());
    }

    @Test
    void checkBalance() {
        Card card = new Card(1L, "1234", 1L, TestUtil.getHash("0000"));
        when(cardsDao.getCardByNumber(anyString())).thenReturn(card);
        when(accountService.checkBalance(1L)).thenReturn(new BigDecimal(1000));

        BigDecimal sum = cardService.getBalance("1234", "0000");
        assertEquals(0, sum.compareTo(new BigDecimal(1000)));
    }

    @Test
    void getMoney() {
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        when(cardsDao.getCardByNumber("1111"))
                .thenReturn(new Card(1L, "1111", 100L, TestUtil.getHash("0000")));

        when(accountService.getMoney(idCaptor.capture(), amountCaptor.capture()))
                .thenReturn(BigDecimal.TEN);

        cardService.getMoney("1111", "0000", BigDecimal.ONE);

        verify(accountService, only()).getMoney(anyLong(), any());
        assertEquals(BigDecimal.ONE, amountCaptor.getValue());
        assertEquals(100L, idCaptor.getValue().longValue());
    }

    @Test
    void putMoney() {
        String pin = "0000";

        Card card = new Card(1L, "1234", 25L, TestUtil.getHash(pin));
        BigDecimal addedDecimal = BigDecimal.TEN;

        when(cardsDao.getCardByNumber("1111"))
                .thenReturn(null);
        when(cardsDao.getCardByNumber("1234"))
                .thenReturn(card);
        when(accountService.putMoney(25L, addedDecimal)).thenReturn(addedDecimal);


        assertThrows(IllegalArgumentException.class, () ->
                cardService.putMoney("1111", pin, addedDecimal));
        assertEquals(addedDecimal,
                cardService.putMoney("1234", pin, addedDecimal));

    }

    @Test
    void changePin() {
        String pin = "0000";
        String newPin = "0001";

        Card card = new Card(1L, "1234", 25L, TestUtil.getHash(pin));

        when(cardsDao.getCardByNumber("1111"))
                .thenReturn(null);
        when(cardsDao.getCardByNumber("1234"))
                .thenReturn(card);

        assertThrows(IllegalArgumentException.class, () ->
                cardService.cnangePin("1111", pin, newPin));
        assertTrue(cardService.cnangePin("1234", pin, newPin));
        assertFalse(cardService.cnangePin("1234", pin, newPin));

    }

    @Test
    void checkIncorrectPin() {
        Card card = new Card(1L, "1234", 1L, "0000");
        when(cardsDao.getCardByNumber(eq("1234"))).thenReturn(card);

        Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
            cardService.getBalance("1234", "0012");
        });
        assertEquals(thrown.getMessage(), "Pincode is incorrect");
    }
}