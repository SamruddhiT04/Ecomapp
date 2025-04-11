package dao;

import entity.model.Customer;
import entity.model.Product;
import exception.ProductNotFoundException;
import util.DBConnUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderProcessorRepositoryImpl implements OrderProcessorRepository {

	public Product getProductByName(String name) throws ProductNotFoundException {
	    String query = "SELECT * FROM products WHERE name = ?";
	    try (Connection conn = DBConnUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, name);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            return new Product(
	                rs.getInt("product_id"),
	                rs.getString("name"),
	                rs.getDouble("price"),
	                rs.getString("description"),
	                rs.getInt("stockQuantity")
	            );
	        } else {
	            throw new ProductNotFoundException("Product with name '" + name + "' not found.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new ProductNotFoundException("Database error occurred while fetching product: " + e.getMessage());
	    }
	}



    public boolean addToCart(int customerId, int productId, int quantity) {
        String sql = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addToCart(Customer customer, Product product, int quantity) {
        return addToCart(customer.getCustomerId(), product.getProductId(), quantity);
    }

    public Customer getCustomerByEmail(String email) {
        Customer customer = null;
        String query = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                customer = new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customer;
    }

    @Override
    public boolean placeOrder(Customer customer, Map<Product, Integer> productQtyMap, String shippingAddress) {
        String orderSQL = "INSERT INTO orders (customer_id, order_date, total_price, shipping_address) VALUES (?, NOW(), ?, ?)";
        String itemSQL = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
        double totalPrice = 0.0;

        for (Map.Entry<Product, Integer> entry : productQtyMap.entrySet()) {
            totalPrice += entry.getKey().getPrice() * entry.getValue();
        }

        try (Connection conn = DBConnUtil.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement orderStmt = conn.prepareStatement(orderSQL, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, customer.getCustomerId());
            orderStmt.setDouble(2, totalPrice);
            orderStmt.setString(3, shippingAddress);
            orderStmt.executeUpdate();

            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            PreparedStatement itemStmt = conn.prepareStatement(itemSQL);
            for (Map.Entry<Product, Integer> entry : productQtyMap.entrySet()) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, entry.getKey().getProductId());
                itemStmt.setInt(3, entry.getValue());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (name, description, price, stockQuantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


   
    @Override
    public boolean createCustomer(Customer customer) {
        String sql = "INSERT INTO customers(name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPassword());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("❌ Duplicate email: " + customer.getEmail());
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    @Override
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeFromCart(Customer customer, Product product) {
        String sql = "DELETE FROM cart WHERE customer_id = ? AND product_id = ?";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customer.getCustomerId());
            ps.setInt(2, product.getProductId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Product> getAllFromCart(Customer customer) {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.description, p.price, p.stock " +
                     "FROM cart c JOIN products p ON c.product_id = p.product_id " +
                     "WHERE c.customer_id = ?";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customer.getCustomerId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStockQuantity(rs.getInt("stock"));
                productList.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

   
    @Override
    public List<Map<Product, Integer>> getOrdersByCustomer(int customerId) {
        List<Map<Product, Integer>> ordersList = new ArrayList<>();

        String orderQuery = "SELECT order_id FROM orders WHERE customer_id = ?";
        String itemQuery = "SELECT oi.product_id, oi.quantity, p.name, p.price, p.description, p.stockQuantity " +
                           "FROM order_items oi JOIN products p ON oi.product_id = p.product_id " +
                           "WHERE oi.order_id = ?";

        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderQuery)) {

            orderStmt.setInt(1, customerId);
            ResultSet orderRs = orderStmt.executeQuery();

            while (orderRs.next()) {
                int orderId = orderRs.getInt("order_id");
                PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
                itemStmt.setInt(1, orderId);
                ResultSet itemRs = itemStmt.executeQuery();

                Map<Product, Integer> productMap = new HashMap<>();

                while (itemRs.next()) {
                    Product product = new Product(
                            itemRs.getInt("product_id"),
                            itemRs.getString("name"),
                            itemRs.getDouble("price"),
                            itemRs.getString("description"),
                            itemRs.getInt("stockQuantity")
                    );
                    int quantity = itemRs.getInt("quantity");
                    productMap.put(product, quantity);
                }

                if (!productMap.isEmpty()) {
                    ordersList.add(productMap);
                }

                itemStmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ordersList;
    }


    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Connection conn = DBConnUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description"),
                    rs.getInt("stockQuantity")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
} 