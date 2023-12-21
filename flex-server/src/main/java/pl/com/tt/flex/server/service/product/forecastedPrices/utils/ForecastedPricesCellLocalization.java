package pl.com.tt.flex.server.service.product.forecastedPrices.utils;

public enum ForecastedPricesCellLocalization {
    FORECASTED_PRICES_DATE(0, 1),
    PRODUCT_NAME(1, 1),
    DAY_HOURS_FIRST_CELL(2, 1),
    PRICE_FIRST_CELL(3, 1),
    EXTRA_HOUR_HEADER(3, 25),
    EXTRA_HOUR_PRICE(3, 25),
    SPRING_FORWARD_PRICE(3, 3), // timestamp 3 (godzina 2:00-2:59)
    SECTION_SIZE(4, 26);

    private final Integer adminRow;
    private final Integer adminCol;

    ForecastedPricesCellLocalization(Integer adminRow, Integer adminCol) {
        this.adminRow = adminRow;
        this.adminCol = adminCol;
    }

    public int getRowNumber() {
        return adminRow;
    }

    public int getColNumber() {
        return adminCol;
    }
}
