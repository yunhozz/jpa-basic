package jpabasic.jpql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    private String name;

    private int price;

    private int stockAmount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStockAmount() {
        return stockAmount;
    }

    public void setStockAmount(int stockAmount) {
        this.stockAmount = stockAmount;
    }
}
