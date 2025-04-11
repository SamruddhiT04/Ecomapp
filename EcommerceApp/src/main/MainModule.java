package main;

import java.util.*;

import dao.OrderProcessorRepository;
import dao.OrderProcessorRepositoryImpl;
import entity.model.Customer;
import entity.model.Product;
import exception.CustomerNotFoundException;
import exception.ProductNotFoundException;

public class MainModule {
    public static void main(String[] args) throws CustomerNotFoundException, ProductNotFoundException {
        Scanner scanner = new Scanner(System.in);
        OrderProcessorRepository repo = new OrderProcessorRepositoryImpl();

        while (true) {
            System.out.println("\n--- E-Commerce Application ---");
            System.out.println("1. Register Customer");
            System.out.println("2. Create Product");
            System.out.println("3. Delete Product");
            System.out.println("4. Add to Cart");
            System.out.println("5. View Cart");
            System.out.println("6. Place Order");
            System.out.println("7. View Customer Orders");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // clear buffer
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter customer name: ");
                        scanner.nextLine();
                        String cname = scanner.nextLine();
                        System.out.print("Enter email: ");
                        String email = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String pass = scanner.nextLine();

                        Customer customer = new Customer();
                        customer.setName(cname);
                        customer.setEmail(email);
                        customer.setPassword(pass);

                        if (repo.createCustomer(customer))
                            System.out.println("Customer registered successfully.");
                        else
                            System.out.println("Failed to register customer.");
                        break;

                    case 2:
                        System.out.print("Enter product name: ");
                        scanner.nextLine();
                        String pname = scanner.nextLine();
                        System.out.print("Enter price: ");
                        double price = scanner.nextDouble();
                        System.out.print("Enter description: ");
                        scanner.nextLine();
                        String desc = scanner.nextLine();
                        System.out.print("Enter stock quantity: ");
                        int stock = scanner.nextInt();

                        Product product = new Product();
                        product.setName(pname);
                        product.setPrice(price);
                        product.setDescription(desc);
                        product.setStockQuantity(stock);

                        if (repo.createProduct(product))
                            System.out.println("Product created successfully.");
                        else
                            System.out.println("Failed to create product.");
                        break;

                    case 3:
                        System.out.print("Enter product ID to delete: ");
                        int delProdId = scanner.nextInt();
                        if (repo.deleteProduct(delProdId))
                            System.out.println("Product deleted successfully.");
                        else
                            System.out.println("Product not found.");
                        break;

                    case 4:
                        System.out.print("Enter customer ID: ");
                        int custId = scanner.nextInt();
                        System.out.print("Enter product ID: ");
                        int prodId = scanner.nextInt();
                        System.out.print("Enter quantity: ");
                        int qty = scanner.nextInt();

                        Customer cust = new Customer();
                        cust.setCustomerId(custId);

                        Product prod = new Product();
                        prod.setProductId(prodId);

                        if (repo.addToCart(cust, prod, qty))
                            System.out.println("Product added to cart.");
                        else
                            System.out.println("Failed to add to cart.");
                        break;

                    case 5:
                        System.out.print("Enter customer ID: ");
                        int viewCustId = scanner.nextInt();

                        Customer viewCust = new Customer();
                        viewCust.setCustomerId(viewCustId);

                        List<Product> cartItems = repo.getAllFromCart(viewCust);
                        if (cartItems.isEmpty()) {
                            System.out.println("Cart is empty.");
                        } else {
                            System.out.println("Cart Contents:");
                            for (Product p : cartItems) {
                                System.out.println(p.getProductId() + " - " + p.getName() + " - ₹" + p.getPrice());
                            }
                        }
                        break;

                    case 6:
                        System.out.print("Enter customer ID: ");
                        int ordCustId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Enter shipping address: ");
                        String address = scanner.nextLine();

                        Customer ordCust = new Customer();
                        ordCust.setCustomerId(ordCustId);

                        List<Product> orderItems = repo.getAllFromCart(ordCust);
                        Map<Product, Integer> productQtyMap = new HashMap<>();
                        for (Product p : orderItems) {
                            System.out.print("Enter quantity for product ID " + p.getProductId() + ": ");
                            int orderQty = scanner.nextInt();
                            productQtyMap.put(p, orderQty);
                        }

                        if (repo.placeOrder(ordCust, productQtyMap, address))
                            System.out.println("Order placed successfully.");
                        else
                            System.out.println("Failed to place order.");
                        break;

                    case 7:
                        System.out.print("Enter customer ID to view orders: ");
                        int viewOrdCustId = scanner.nextInt();
                        List<Map<Product, Integer>> orders = repo.getOrdersByCustomer(viewOrdCustId);
                        if (orders.isEmpty()) {
                            System.out.println("No orders found.");
                        } else {
                            System.out.println("Customer Orders:");
                            for (Map<Product, Integer> order : orders) {
                                System.out.println(order);
                            }
                        }
                        break;

                    case 8:
                        System.out.println("Thank you for using the E-Commerce App!");
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (Exception e) {
                System.out.println("Unexpected Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}