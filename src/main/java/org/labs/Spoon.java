package org.labs;

public class Spoon {
    volatile private boolean onTable = true;

    public boolean isOnTable() {
        return onTable;
    }

    public void pickMe() {
        onTable = false;
    }

    public void putMeBack() {
        onTable = true;
    }
}
