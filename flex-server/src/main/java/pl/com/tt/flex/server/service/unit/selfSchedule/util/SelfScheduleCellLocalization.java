package pl.com.tt.flex.server.service.unit.selfSchedule.util;

public enum SelfScheduleCellLocalization {
    UNIT_NAME(2, 1, 1, 1),
    SELF_SCHEDULE_DATE(1, 1, 0, 1),
    DAY_HOURS_FIRST_CELL(3, 1, 2, 1),
    POWER_FIRST_CELL(4, 1, 3, 1),
    FSP_COMPANY_NAME(0, 1, null, null),
    EXTRA_HOUR_HEADER(4, 25, 3, 25),
    EXTRA_HOUR_PRICE(4, 25, 3, 25),
    SECTION_SIZE(5, 26, 4, 26);

    private final Integer adminRow;
    private final Integer adminCol;
    private final Integer userRow;
    private final Integer userCol;

    SelfScheduleCellLocalization(Integer adminRow, Integer adminCol, Integer userRow, Integer userCol) {
        this.adminRow = adminRow;
        this.adminCol = adminCol;
        this.userRow = userRow;
        this.userCol = userCol;
    }

    public int getRowNumber(boolean isAdmin) {
        return isAdmin ? adminRow : userRow;
    }

    public int getColNumber(boolean isAdmin) {
        return isAdmin ? adminCol : userCol;
    }
}
