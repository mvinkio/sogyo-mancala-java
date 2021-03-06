package mancala.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SmallBowl extends Bowl {

    public static SmallBowl referencePoint;

    public SmallBowl() throws DomainSmallBowlException {
        this(Arrays.stream(new int[] {4,4,4,4,4,4,0,4,4,4,4,4,4,0}).boxed().collect(Collectors.toList()));
        referencePoint = this;
    }

    public SmallBowl(List<Integer> stonesList) throws DomainSmallBowlException {
        if (stonesList.size() % 2 != 0) {
            throw new DomainSmallBowlException("Stones List should contain even number of elements.");
        }
        if (stonesList.size() < 4) {
            throw new DomainSmallBowlException("Stones list should have length greater than or equal to 4.");
        }
        this.myOwner = new Player();

        int boardSize = stonesList.size();
        int bowlsToAdd = boardSize - 1;

        this.myStones = stonesList.remove(0);

        if (boardSize == 4) this.nextBowl = new Kalaha(boardSize, bowlsToAdd, stonesList, this, this.getMyOwner());
        else this.nextBowl = new SmallBowl(boardSize, bowlsToAdd, stonesList, this, this.getMyOwner());

        referencePoint = this;
    }

    SmallBowl(int boardSize, int bowlsToAdd, List<Integer> stonesList, Bowl startBowl, Player playerOwningThisSide) {
        bowlsToAdd = bowlsToAdd - 1;
        this.myOwner = playerOwningThisSide;
        this.myStones = stonesList.remove(0);

        if (bowlsToAdd == 0) nextBowl = startBowl;

        else if (bowlsToAdd == (boardSize / 2) + 1) nextBowl = new Kalaha(boardSize, bowlsToAdd, stonesList, startBowl, playerOwningThisSide);

        else if (bowlsToAdd == 1) nextBowl = new Kalaha(boardSize, bowlsToAdd, stonesList, startBowl, playerOwningThisSide);

        else nextBowl = new SmallBowl(boardSize, bowlsToAdd, stonesList, startBowl, playerOwningThisSide);
    }


    public SmallBowl getNextSmallBowlTimes(int remainingTimes) {
        if (remainingTimes == 0)
            return this;
        else {
            return getNextBowl().getNextSmallBowlTimes(--remainingTimes);
        }
    }

    public void play() {
        if (myOwner.hasTheTurn() == false) return;
        if (isEmpty()) return;

        int passThese = myStones;
        myStones = 0;
        getNextBowl().distribute(passThese);
    }

    @Override
    boolean isEmpty() {
        return this.myStones == 0;
    }

    void distribute(int remainingRocks) {
        this.myStones++;
        // last?
        if (remainingRocks == 1)
            lastSmallBowl();
        else {
            getNextBowl().distribute(--remainingRocks);
        }
    }

    private void lastSmallBowl() {
        // Did play end in smallbowl of my player? steal, otherwise do nothing
        if (getMyOwner().hasTheTurn()) stealTheBooty(false);

        endTheGame();

        getMyOwner().switchTurn();

        endTheGame();
    }

    SmallBowl getNextSmallBowl() {
        return this;
    }

    @Override
    SmallBowl goToFirstBowlOfPlayerWithTurn() {
        if (getMyOwner().hasTheTurn()) {
            return getKalaha().getNextBowl().getKalaha().getNextSmallBowl();
        } else {
            return getKalaha().getNextSmallBowl();
        }
    }

    Kalaha getKalaha() {
        return getNextBowl().getKalaha();
    }

    private void stealTheBooty(boolean victim) {
        if (victim){
            System.out.println("help! i'm being robbed, my precious " + getMyStones() + " stones are gone :(.");
            getOpposite().getKalaha().claimStolenBooty(myStones);
            myStones = 0;

        } else if (getMyStones() == 1 &&
                getOpposite().getMyStones() != 0) {
            System.out.println("stealing");

            getKalaha().claimStolenBooty(myStones);
            myStones = 0;
            getOpposite().stealTheBooty(true);
        }
    }

    SmallBowl getOpposite() {
        return getOpposite(0);
    }

    SmallBowl getOpposite(int count) {
        count = count + 1;
        return getNextBowl().getOpposite(count);
    }

    public String stateString() {
        return SmallBowl.referencePoint.makeString("", "", "");
    }

    protected String makeString(String playerBowls, String opponentBowls, String kalahas) {
        if (!this.getMyOwner().equals(SmallBowl.referencePoint.getMyOwner())) {
            return getNextBowl().makeString(
                    playerBowls,
                    (getMyStones() + ", ") + opponentBowls,
                    kalahas);
        } else {
            return getNextBowl().makeString(
                    playerBowls + (playerBowls.equals("") ? getMyStones() : ", " + getMyStones()),
                    opponentBowls,
                    kalahas);
        }
    }

    @Override
    protected int[] toStateArray(int[] stateArray, int index) {
        stateArray[index] = getMyStones();
        if (index == stateArray.length - 2) {
            return stateArray;
        } else {
            return getNextBowl().toStateArray(stateArray, ++index);
        }
    }
}
