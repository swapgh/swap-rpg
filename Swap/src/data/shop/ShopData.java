package data.shop;

import java.util.List;

public record ShopData(
        List<ShopListingData> dayListings,
        List<ShopListingData> nightListings) {

    public List<ShopListingData> listingsForPhase(boolean dayPhase) {
        return dayPhase ? dayListings : nightListings;
    }

    public record ShopListingData(
            String itemId,
            int price,
            int stock) {
    }
}
