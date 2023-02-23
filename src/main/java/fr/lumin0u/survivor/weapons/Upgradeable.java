package fr.lumin0u.survivor.weapons;

public interface Upgradeable {
    void upgrade();

    int getLevel();

    int getNextLevelPrice();
}
