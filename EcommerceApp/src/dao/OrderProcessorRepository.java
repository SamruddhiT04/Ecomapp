package dao;

import entity.*;
import entity.model.Customer;
import entity.model.Product;

import java.util.List;
import java.util.Map;

public interface OrderProcessorRepository {
    boolean createProduct(Product product);
    boolean createCustomer(Customer customer);
    boolean deleteProduct(int productId);
    boolean deleteCustomer(int customerId);
    boolean addToCart(Customer customer, Product product, int quantity);
    boolean removeFromCart(Customer customer, Product product);
    List<Product> getAllFromCart(Customer customer);
    boolean placeOrder(Customer customer, Map<Product, Integer> productQtyMap, String shippingAddress);
    List<Map<Product, Integer>> getOrdersByCustomer(int customerId);
}
