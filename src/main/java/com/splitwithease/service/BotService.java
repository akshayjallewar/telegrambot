package com.splitwithease.service;

import com.splitwithease.model.Balance;
import com.splitwithease.model.Transaction;
import com.splitwithease.model.TransactionEntity;
import com.splitwithease.repository.BalanceRepository;
import com.splitwithease.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class BotService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    private enum State {
        NONE,
        AWAITING_CREDIT_AMOUNT,
        AWAITING_CREDIT_NAME,
        AWAITING_EXPENSE_DESCRIPTION,
        AWAITING_EXPENSE_AMOUNT
    }

    private final Map<Long, State> userStates = new HashMap<>();
    private final Map<Long, String> tempExpenseDescription = new HashMap<>();
    private final Map<Long, Double> tempCreditAmount = new HashMap<>();

    private Balance getBalanceEntity() {
        return balanceRepository.findById(1L).orElseGet(() -> {
            Balance balance = new Balance();
            balance.setUserId(1L);
            balance.setAmount(0.0);
            return balanceRepository.save(balance);
        });
    }

    public SendMessage handleUpdate(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText().trim();
        String lowerMsg = messageText.toLowerCase();

        State currentState = userStates.getOrDefault(chatId, State.NONE);
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        
        if (lowerMsg.startsWith("/")) {
            userStates.put(chatId, State.NONE);
            currentState = State.NONE;

            switch (lowerMsg) {
                case "/addcredit" -> {
                    userStates.put(chatId, State.AWAITING_CREDIT_AMOUNT);
                    response.setText("💳 How much amount do you wish to *credit*?");
                    response.enableMarkdown(true);
                    return response;
                }
                case "/addexpense" -> {
                    userStates.put(chatId, State.AWAITING_EXPENSE_DESCRIPTION);
                    response.setText("📝 Enter expense *description*:");
                    response.enableMarkdown(true);
                    return response;
                }
                case "/balance" -> {
                    Balance balanceEntity = getBalanceEntity();
                    response.setText("💰 *Current Balance:* ₹" + balanceEntity.getAmount());
                    response.enableMarkdown(true);
                    return response;
                }
                case "/history" -> {
                    var transactions = transactionRepository.findAll();
                    if (transactions.isEmpty()) {
                        response.setText("📭 No transactions found.");
                    } else {
                        StringBuilder history = new StringBuilder("📜 *Transaction History:*\n");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

                        for (TransactionEntity t : transactions) {
                            history.append("\n")
                                    .append(t.getTimestamp().format(formatter)).append(" — ")
                                    .append(t.getType().equals(Transaction.CREDIT) ? "➕" : "➖")
                                    .append(" ₹*").append(t.getAmount()).append("*")
                                    .append(" | *").append(t.getDescription()).append("*");
                        }
                        response.setText(history.toString());
                    }
                    response.enableMarkdown(true);
                    return response;
                }
                default -> {
                    response.setText("🤖 Welcome to SplitWithEase. Try:\n" +
                            "/addCredit — Add credit 💳\n" +
                            "/addExpense — Record an expense 🧾\n" +
                            "/balance — Show balance 💰\n" +
                            "/history — View transaction history 📜");
                    response.enableMarkdown(true);
                    return response;
                }
            }
        }

        switch (currentState) {
            case AWAITING_CREDIT_AMOUNT -> {
                try {
                    double amount = Double.parseDouble(messageText);
                    tempCreditAmount.put(chatId, amount);
                    userStates.put(chatId, State.AWAITING_CREDIT_NAME);
                    response.setText("👤 Who's adding the money?");
                } catch (NumberFormatException e) {
                    response.setText("❌ Please enter a valid number for credit amount.");
                }
            }

            case AWAITING_CREDIT_NAME -> {
                double amount = tempCreditAmount.getOrDefault(chatId, 0.0);
                Balance balanceEntity = getBalanceEntity();
                balanceEntity.setAmount(balanceEntity.getAmount() + amount);
                balanceRepository.save(balanceEntity);

                TransactionEntity credit = new TransactionEntity();
                credit.setType(Transaction.CREDIT);
                credit.setAmount(amount);
                credit.setAddedBy(messageText);
                credit.setDescription("Credit by " + messageText);
                credit.setTimestamp(LocalDateTime.now());
                transactionRepository.save(credit);

                response.setText("✅ ₹" + amount + " credited by *" + messageText + "* 💸\n\n" +
                        "💰 *Current Balance:* ₹" + balanceEntity.getAmount());
                userStates.put(chatId, State.NONE);
                tempCreditAmount.remove(chatId);
            }

            case AWAITING_EXPENSE_DESCRIPTION -> {
                tempExpenseDescription.put(chatId, messageText);
                userStates.put(chatId, State.AWAITING_EXPENSE_AMOUNT);
                response.setText("💰 Enter the amount to be *debited*:");
            }

            case AWAITING_EXPENSE_AMOUNT -> {
                try {
                    double expenseAmount = Double.parseDouble(messageText);
                    Balance balanceEntity = getBalanceEntity();
                    balanceEntity.setAmount(balanceEntity.getAmount() - expenseAmount);
                    balanceRepository.save(balanceEntity);

                    TransactionEntity debit = new TransactionEntity();
                    debit.setType(Transaction.DEBIT);
                    debit.setAmount(expenseAmount);
                    debit.setAddedBy("N/A");
                    debit.setDescription(tempExpenseDescription.get(chatId));
                    debit.setTimestamp(LocalDateTime.now());
                    transactionRepository.save(debit);

                    response.setText("🧾 Expense: *" + tempExpenseDescription.get(chatId) + "*\n" +
                            "💸 Amount: ₹" + expenseAmount + "\n\n" +
                            "💰 *Current Balance:* ₹" + balanceEntity.getAmount());

                    userStates.put(chatId, State.NONE);
                    tempExpenseDescription.remove(chatId);
                } catch (NumberFormatException e) {
                    response.setText("❌ Please enter a valid number for expense.");
                }
            }
            
            default -> {
                response.setText("🤖 Welcome to SplitWithEase. Try:\n" +
                        "/addCredit — Add credit 💳\n" +
                        "/addExpense — Record an expense 🧾\n" +
                        "/balance — Show balance 💰\n" +
                        "/history — View transaction history 📜");
            }
        }

        response.enableMarkdown(true);
        return response;
    }
}
