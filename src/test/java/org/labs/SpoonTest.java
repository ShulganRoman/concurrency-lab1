package org.labs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpoonTest {

    @Test
    void newSpoonIsOnTable() {
        Spoon spoon = new Spoon();

        assertTrue(spoon.isOnTable());
    }

    @Test
    void pickMeAndPutMeBackToggleState() {
        Spoon spoon = new Spoon();

        spoon.pickMe();
        assertFalse(spoon.isOnTable());

        spoon.putMeBack();
        assertTrue(spoon.isOnTable());
    }
}
