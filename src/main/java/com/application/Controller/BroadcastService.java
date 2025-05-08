package com.application.Controller;

public class BroadcastService {
    void startBroadcastScheduler(); // Запуск всей логики при старте бота
    long computeInitialDelayUntilNextBroadcast(ZoneOffset offset, LocalTime targetTime);
    private void sendBroadcastToAllUsers() {

    } // Отправляет сообщения всем пользователям
    boolean hasBroadcastBeenSentToday(LocalDate date); // Проверяет, была ли рассылка сегодня
    void markBroadcastAsSent(LocalDate date); // Отмечает, что рассылка выполнена
    LocalDate getTodayAtOffset(ZoneOffset offset); // Возвращает дату с учётом смещения UTC+3
}
