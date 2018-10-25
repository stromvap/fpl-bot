package fpl.bot.api.fplstatistics;

import java.util.Objects;

public class Player {
    private String name;
    private double price;
    private double priceChangePercentage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceChangePercentage() {
        return priceChangePercentage;
    }

    public void setPriceChangePercentage(double priceChangePercentage) {
        this.priceChangePercentage = priceChangePercentage;
    }

    public boolean isAboutToRise() {
        return priceChangePercentage >= FplStatisticsService.THRESHOLD;
    }

    @Override
    public String toString() {
        return "Player{" + "name='" + name + '\'' + ", price=" + price + ", priceChangePercentage=" + priceChangePercentage + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Double.compare(player.price, price) == 0 && Double.compare(player.priceChangePercentage, priceChangePercentage) == 0 && Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, priceChangePercentage);
    }
}
