package fpl.bot.api.fplstatistics;

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
}
