package com.lgsdiamond.blackjackstudio.BlackjackElement;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public abstract class Person {
    protected BjService mService;
    protected String mName;
    protected double mBalance;
    protected double mBalanceInitial;
    protected double balance;

    Person(BjService service, String name, double balance) {
        mService = service;
        mName = name;
        mBalance = mBalanceInitial = balance;
    }

    String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    //=== getter ===
    public BjService getService() {
        return mService;
    }

    public double getBalance() {
        return mBalance;
    }

    public void setBalance(double balance) {
        mBalance = balance;
    }

    public double getBalanceInitial() {
        return mBalanceInitial;
    }

    public void setBalanceInitial(double balanceInitial) {
        mBalanceInitial = balanceInitial;
    }

    public double getBalanceChange() {
        return (mBalance - mBalanceInitial);
    }

    //=== paying ===
    public void takeOutBalance(double amount) {
        if (amount == 0.0) return;

        mBalance -= amount;

        updateGameData_Balance();
    }

    public void putInBalance(double amount) {
        if (amount == 0.0) return;

        mBalance += amount;
        if (!BjService.isAutoRunning())
            updateGameData_Balance();
    }

    abstract void updateGameData_Balance();

    public void resetBalance() {
        mBalance = mBalanceInitial;
    }

    //=== Testing ===
    public void ResetBankroll() {
        mBalanceInitial += BjService.DEFAULT_BANKROLL;
        mBalance += BjService.DEFAULT_BANKROLL;

        if (!BjService.isAutoRunning())
            BjService.sGameData.setBankRoll(getBalance());
    }

}