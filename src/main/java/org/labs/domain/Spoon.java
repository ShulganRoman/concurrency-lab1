package org.labs.domain;

public class Spoon {
    private boolean onTable = true;

    public boolean isOnTable() {
        return onTable;
    }

    public void takeSpoon() {
        if (!onTable)
            throw new IllegalStateOfSpoonException();

        onTable = false;
    }

    public void putSpoon() {
        if (onTable)
            throw new IllegalStateOfSpoonException();

        onTable = true;
    }
}
