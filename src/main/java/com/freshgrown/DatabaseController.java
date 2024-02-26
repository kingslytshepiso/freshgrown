package com.freshgrown;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DatabaseController {
    private Connection c = null;

    public void connect() {

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:main.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /** Remove contents of the cart */
    public void clearCart() {
        try {
            connect();
            String sql = "Delete From cart_item";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.execute();
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public User login(String username, String password) {
        try {
            connect();
            String sql = "select * from user where username=? and password=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setString(1, username);
            preparedStmt.setString(2, password);
            ResultSet rs = preparedStmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.userId = rs.getInt("userId");
                user.username = rs.getString("username");
                user.password = rs.getString("password");
                user.name = rs.getString("name");
                user.surname = rs.getString("surname");
                c.close();
                createSession("$-cashier-$-session", user.userId);
                return user;
            }
            ;
            c.close();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean insertSlipItems(ArrayList<SlipItem> items) {
        try {
            for (int i = 0; i < items.size(); i++) {
                connect();
                String sql = "Insert Into slip_item values(?,?,?,?)";
                PreparedStatement preparedStmt = c.prepareStatement(sql);
                preparedStmt.setString(1, null);
                preparedStmt.setString(2, items.get(i).itemName);
                preparedStmt.setDouble(3, items.get(i).price);
                preparedStmt.setInt(4, items.get(i).quantity);
                preparedStmt.execute();
                c.close();
            }
            return true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Boolean updateInventory(ArrayList<CartItem> items) {
        var inventory = getProducts();
        Boolean canProceed = true;
        for (int i = 0; i < items.size(); i++) {
            for (int j = 0; j < inventory.size(); j++) {
                if (items.get(i).barcode == inventory.get(j).getBarcode()) {
                    if (items.get(i).quantity > inventory.get(j).getQuantityAvailable()) {
                        canProceed = false;
                    }
                }
            }
        }
        if (canProceed) {
            try {
                for (int i = 0; i < items.size(); i++) {
                    connect();
                    String sql = "Update product Set available=available - ? where barcode=?";
                    PreparedStatement preparedStmt = c.prepareStatement(sql);
                    preparedStmt.setInt(1, items.get(i).quantity);
                    preparedStmt.setDouble(2, items.get(i).barcode);
                    preparedStmt.execute();
                    c.close();
                }
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public Boolean createSlip(ArrayList<SlipItem> slipItems, ArrayList<CartItem> cartItems) {
        clearSlip();
        Boolean isInventorySynced = updateInventory(cartItems);
        if (isInventorySynced) {
            return insertSlipItems(slipItems);
        }
        return false;
    }

    public User getUserWithId(Integer userId) {
        try {
            connect();
            String sql = "Select * from user where userId=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, userId);
            ResultSet rs = preparedStmt.executeQuery();
            User user = new User();
            if (rs.next()) {
                user.userId = rs.getInt("userId");
                user.username = rs.getString("username");
                user.password = rs.getString("password");
                user.name = rs.getString("name");
                user.surname = rs.getString("surname");
            }
            ;
            c.close();
            return user;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public SessionObject getSession() {
        try {
            connect();
            String sql = "Select * from session";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            ResultSet rs = preparedStmt.executeQuery();
            SessionObject sess = new SessionObject();
            if (rs.next()) {
                sess.sessionId = rs.getString("sessionId");
                sess.userId = rs.getInt("userId");
            }
            ;
            c.close();
            return sess;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public User getCurrentUser() {
        SessionObject sess = getSession();
        try {
            connect();
            String sql = "Select * from user where userId=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, sess.userId);
            ResultSet rs = preparedStmt.executeQuery();
            User user = new User();
            if (rs.next()) {
                user.userId = rs.getInt("userId");
                user.username = rs.getString("username");
                user.password = rs.getString("password");
                user.name = rs.getString("name");
                user.surname = rs.getString("surname");
            }
            ;
            c.close();
            return user;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    public Boolean executePayment(ArrayList<SlipItem> items,
            Double totalAmount, Double change, Integer cardNumber,
            String cashierName) {
        try {
            connect();
            String sql = "Update transaction_item Set change=?, totalCost=?, bankCardNo=?, cashierName=?, payment_status=? where id=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setDouble(1, change);
            preparedStmt.setDouble(2, totalAmount);
            preparedStmt.setDouble(3, cardNumber);
            preparedStmt.setString(4, cashierName);
            preparedStmt.setString(5, "paid");
            preparedStmt.setInt(6, App.currentTransactionId);
            preparedStmt.execute();
            c.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void createSession(String sessionId, int userId) {
        try {
            connect();
            String sql = "Update session Set sessionId=?, userId=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setString(1, sessionId);
            preparedStmt.setInt(2, userId);
            preparedStmt.execute();
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<CartItem> getTransactionItemsWithId(Integer id) {
        ArrayList<CartItem> items = new ArrayList<CartItem>();
        try {
            connect();
            String sql = "Select p.barcode, p.name, p.price " +
                    "From product p, transaction_product t " +
                    "Where t.transactionId = ? And t.productCode = p.barcode";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, id);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                var item = new CartItem();
                item.barcode = rs.getInt("barcode");
                item.name = rs.getString("name");
                item.price = rs.getDouble("price");
                items.add(item);
            }
            ;
            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return items;
    }

    public ArrayList<Transaction> getTransactions() {
        ArrayList<Transaction> items = new ArrayList<Transaction>();
        try {
            connect();
            String sql = "Select * from transaction_item where payment_status='paid'";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                var item = new Transaction();
                item.id = rs.getInt("id");
                item.date = rs.getString("date");
                item.cashierId = rs.getInt("cashierId");
                item.amount = rs.getDouble("amount");
                item.paymentStatus = rs.getString("payment_status");
                item.change = rs.getDouble("change");
                item.bankCardNo = rs.getLong("bankCardNo");
                item.totalCost = rs.getDouble("totalCost");
                item.cashierName = rs.getString("cashierName");
                items.add(item);
            }
            ;
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return items;
    }

    public Transaction getTransactionWithId(Integer id) {
        try {
            connect();
            String sql = "Select * from transaction_item Where id=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, id);
            ResultSet rs = preparedStmt.executeQuery();
            var transaction = new Transaction();

            if (rs.next()) {
                transaction.id = rs.getInt("id");
                transaction.date = rs.getString("date");
                transaction.cashierId = rs.getInt("cashierId");
                transaction.amount = rs.getDouble("amount");
                transaction.paymentStatus = rs.getString("payment_status");
                transaction.change = rs.getDouble("change");
                transaction.bankCardNo = rs.getLong("bankCardNo");
                transaction.totalCost = rs.getDouble("totalCost");
                transaction.cashierName = rs.getString("cashierName");
            }
            ;
            c.close();
            return transaction;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Extract all current cart items from the database
     * 
     * @return An array list of cart items that are loaded in to the system
     */
    public ArrayList<CartItem> getCart() {
        ArrayList<CartItem> items = new ArrayList<CartItem>();
        try {
            connect();
            String sql = "Select * from cart_item";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                var item = new CartItem();
                item.barcode = rs.getInt("barcode");
                item.name = rs.getString("name");
                item.price = rs.getDouble("price");
                item.quantity = rs.getInt("quantity");
                items.add(item);
            }
            ;
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return items;
    }

    public Transaction getCurrentTransaction() {
        try {
            connect();
            String sql = "Select * from transaction_item Where id=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, App.currentTransactionId);
            ResultSet rs = preparedStmt.executeQuery();
            var transaction = new Transaction();

            if (rs.next()) {
                transaction.id = rs.getInt("id");
                transaction.date = rs.getString("date");
                transaction.cashierId = rs.getInt("cashierId");
                transaction.amount = rs.getDouble("amount");
                transaction.paymentStatus = rs.getString("payment_status");
                transaction.change = rs.getDouble("change");
                transaction.bankCardNo = rs.getLong("bankCardNo");
                transaction.totalCost = rs.getDouble("totalCost");
                transaction.cashierName = rs.getString("cashierName");
            }
            ;
            c.close();
            return transaction;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Remove the current session and log out from the system.
     */
    public void destroySession() {
        clearCart();
        clearSlip();
        try {
            connect();
            String sql = "Update session Set sessionId='none', userId=999";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.execute();
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<SlipItem> getSlip() {
        try {
            connect();
            String sql = "Select * from slip_item";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            ResultSet rs = preparedStmt.executeQuery();
            ArrayList<SlipItem> items = new ArrayList<SlipItem>();
            while (rs.next()) {
                var item = new SlipItem();
                item.id = rs.getInt("id");
                item.itemName = rs.getString("name");
                item.quantity = rs.getInt("quantity");
                item.price = rs.getDouble("price");
                items.add(item);
            }
            ;
            c.close();
            return items;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ArrayList<SlipItem>();
        }
    }

    public void clearSlip() {
        try {
            connect();
            String sql = "Delete From slip_item";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.execute();
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Integer captureTransaction(Double totalAmount) {
        Integer transactionId = insertTransaction(totalAmount);
        if (transactionId > 0) {
            insertTransactionProduct(transactionId);
        }
        return transactionId;
    }

    public Integer insertTransaction(Double totalAmount) {
        try {
            connect();
            LocalDate date = LocalDate.now();
            String sql = "Insert Into transaction_item values(?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setString(1, null);
            preparedStmt.setString(2, date.toString());
            preparedStmt.setInt(3, 444123);
            preparedStmt.setDouble(4, totalAmount);
            preparedStmt.setString(5, "unpaid");
            preparedStmt.setString(6, null);
            preparedStmt.setString(7, null);
            preparedStmt.setString(8, null);
            preparedStmt.setString(9, null);
            preparedStmt.execute();
            ResultSet rs = preparedStmt.getGeneratedKeys();
            Integer id = 0;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            c.close();
            return id;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public void insertTransactionProduct(Integer transactionId) {
        ArrayList<CartItem> items = getCart();
        for (int i = 0; i < items.size(); i++) {
            try {
                connect();
                String sql = "Insert Into transaction_product values(?,?,?)";
                PreparedStatement preparedStmt = c.prepareStatement(sql);
                preparedStmt.setString(1, null);
                preparedStmt.setInt(2, transactionId);
                preparedStmt.setInt(3, items.get(i).barcode);
                preparedStmt.execute();
                c.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public Boolean insertToCart(Integer barcode, String name, Integer quantity, Double price) {
        try {
            connect();
            String sql = "Insert Into cart_item values(?,?,?,?)";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, barcode);
            preparedStmt.setString(2, name);
            preparedStmt.setInt(3, quantity);
            preparedStmt.setDouble(4, price);
            preparedStmt.execute();
            c.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void insertToCart(Integer barcode) {
        try {
            connect();
            String sql = "Update cart_item Set quantity=quantity + 1 where barcode=?";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, barcode);
            preparedStmt.execute();
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeFromCart(Integer barcode, Integer available) {
        try {
            connect();
            String sql;
            if (available <= 1) {
                sql = "Delete From cart_item where barcode=?";
            } else {
                sql = "Update cart_item Set quantity=quantity - 1 Where barcode=?";
            }
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            preparedStmt.setInt(1, barcode);
            preparedStmt.execute();
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<Product> getProducts() {
        ArrayList<Product> products = new ArrayList<Product>();
        try {
            connect();
            String sql = "Select * from product";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                var product = new Product();
                product.SetBarcode(rs.getInt("barcode"));
                product.SetName(rs.getString("name"));
                product.SetDescription(rs.getString("description"));
                product.SetPrice(rs.getDouble("price"));
                product.SetAvailable(rs.getInt("available"));
                product.SetType(rs.getString("type"));
                products.add(product);
            }
            ;
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    public ArrayList<Product> searchProducts(String keyword) {
        ArrayList<Product> products = new ArrayList<Product>();
        try {
            connect();
            String sql = "Select * from product";
            PreparedStatement preparedStmt = c.prepareStatement(sql);
            ResultSet rs = preparedStmt.executeQuery();
            while (rs.next()) {
                String barcodeStr = rs.getString("barcode").toLowerCase();
                String nameStr = rs.getString("name").toLowerCase();
                if (barcodeStr.indexOf(keyword) >= 0 || nameStr.indexOf(keyword) >= 0) {
                    var product = new Product();
                    product.SetBarcode(rs.getInt("barcode"));
                    product.SetName(rs.getString("name"));
                    product.SetDescription(rs.getString("description"));
                    product.SetPrice(rs.getDouble("price"));
                    product.SetAvailable(rs.getInt("available"));
                    product.SetType(rs.getString("type"));
                    products.add(product);
                }

            }
            ;
            c.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return products;
    }
}
