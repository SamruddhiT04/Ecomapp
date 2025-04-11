package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import dao.OrderProcessorRepositoryImpl;
import entity.model.Customer;
import entity.model.Product;
import exception.ProductNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcommerceTest {

    private OrderProcessorRepositoryImpl orderProcessorRepository;

    @Before
    public void setUp() {
        orderProcessorRepository = new OrderProcessorRepositoryImpl();
    }

    @Test
    public void testCreateCustomer() {
        Customer customer = new Customer(0, "David Tester", "david@example.com", "test1234");
        boolean result = orderProcessorRepository.createCustomer(customer);
        assertTrue("Customer should be created successfully", result);
    }

    @Test
    public void testGetAllProducts() {
        List<Product> products = orderProcessorRepository.getAllProducts();
        assertNotNull("Product list should not be null", products);
        assertTrue("Product list should not be empty", products.size() > 0);
    }

    @Test
    public void testGetProductByNameSuccess() {
        try {
            Product product = orderProcessorRepository.getProductByName("Laptop");
            assertNotNull("Product should be found", product);
            assertEquals("Laptop", product.getName());
        } catch (ProductNotFoundException e) {
            fail("Product should exist, but exception was thrown: " + e.getMessage());
        }
    }

    @Test(expected = ProductNotFoundException.class)
    public void testGetProductByNameFailure() throws ProductNotFoundException {
        orderProcessorRepository.getProductByName("NonExistingProduct");
    }

    @Test
    public void testAddToCart() {
        boolean result = orderProcessorRepository.addToCart(1, 2, 1); // customer_id=1, product_id=2
        assertTrue("Item should be added to cart", result);
    }

    @Test
    public void testPlaceOrder() throws ProductNotFoundException {
        Customer customer = new Customer(2, "Jane Doe", "jane@example.com", "password123");

        Map<Product, Integer> productQtyMap = new HashMap<>();
        Product product = orderProcessorRepository.getProductByName("Laptop");
		productQtyMap.put(product, 1);

		boolean result = orderProcessorRepository.placeOrder(customer, productQtyMap, "789 Test Blvd");
		assertTrue("Order should be placed successfully", result);
    }

    @Test
    public void testGetOrdersByCustomerId() {
        List<Map<Product, Integer>> orders = orderProcessorRepository.getOrdersByCustomer(1);
        assertNotNull("Orders should not be null", orders);
    }
}
