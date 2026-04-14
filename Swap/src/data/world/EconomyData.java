package data.world;

import data.shop.ShopData;
import java.util.Map;

public record EconomyData(
        EconomyPhaseData day,
        EconomyPhaseData night) {

    public EconomyPhaseData phase(boolean dayPhase) {
        return dayPhase ? day : night;
    }

    public EffectiveShopListing apply(ShopData.ShopListingData base, boolean dayPhase) {
        EconomyPhaseData phase = phase(dayPhase);
        ItemEconomyData itemRule = phase.items().get(base.itemId());
        int adjustedPrice = Math.max(1, Math.round(base.price() * phase.priceMultiplier()));
        int adjustedStock = base.stock();

        if (itemRule != null) {
            adjustedPrice = Math.max(1, Math.round(adjustedPrice * itemRule.priceMultiplier()));
            if (adjustedStock >= 0) {
                adjustedStock = Math.max(0, adjustedStock + itemRule.stockDelta());
            }
        }

        if (adjustedStock >= 0) {
            adjustedStock = Math.max(0, adjustedStock + phase.stockDelta());
        }
        return new EffectiveShopListing(base.itemId(), adjustedPrice, adjustedStock);
    }

    public record EconomyPhaseData(
            float priceMultiplier,
            int stockDelta,
            Map<String, ItemEconomyData> items) {
    }

    public record ItemEconomyData(
            float priceMultiplier,
            int stockDelta) {
    }

    public record EffectiveShopListing(
            String itemId,
            int price,
            int stock) {
    }
}
