package test;

import static org.junit.Assert.*;

import dao.OrderProcessorRepositoryImpl;
import entity.model.Customer;
import entity.model.Product;
import exception.ProductNotFoundException;
import org.junit.Before;
import org.junit.Test;

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
        String email = "david_" + System.currentTimeMillis() + "@example.com";
        Customer customer = new Customer(0, "David Tester", email, "test1234");
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
        boolean result = orderProcessorRepository.addToCart(1, 2, 1); // assume customer_id=1, product_id=2 exist
        assertTrue("Item should be added to cart", result);
    }

    @Test
    public void testPlaceOrder() throws ProductNotFoundException {
        // Step 1: Create a new customer
        String email = "jane_" + System.currentTimeMillis() + "@example.com";
        Customer customer = new Customer(0, "Jane Doe", email, "password123");
        boolean isCreated = orderProcessorRepository.createCustomer(customer);
        assertTrue("Customer creation should succeed", isCreated);

        // Step 2: Get the inserted customer ID (you must have this method)
        customer = orderProcessorRepository.getCustomerByEmail(email);
        assertNotNull("Inserted customer should be fetched", customer);

        // Step 3: Get product
        Product product = orderProcessorRepository.getProductByName("Laptop");
        assertNotNull("Product should be found", product);

        // Step 4: Prepare cart
        Map<Product, Integer> productQtyMap = new HashMap<>();
        productQtyMap.put(product, 1);

        // Step 5: Place order
        boolean result = orderProcessorRepository.placeOrder(customer, productQtyMap, "789 Test Blvd");
        assertTrue("Order should be placed successfully", result);
    }

    @Test
    public void testGetOrdersByCustomerId() {
        List<Map<Product, Integer>> orders = orderProcessorRepository.getOrdersByCustomer(1);
        assertNotNull("Orders should not be null", orders);
    }
}
