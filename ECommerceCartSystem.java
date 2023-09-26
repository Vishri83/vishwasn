import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Product {
    private final String name;
    private final double price;
    private final boolean available;

    public Product(String name, double price, boolean available) {
        this.name = name;
        this.price = price;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }
}

interface DiscountStrategy {
    double applyDiscount(double originalPrice, int quantity);
}

class PercentageDiscountStrategy implements DiscountStrategy {
    private final double discountPercentage;

    public PercentageDiscountStrategy(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double applyDiscount(double originalPrice, int quantity) {
        return originalPrice * (1 - discountPercentage) * quantity;
    }
}

class ShoppingCart {
    private final Map<Product, Integer> cart = new HashMap<>();
    private DiscountStrategy discountStrategy;
    private final List<Product> availableProducts;

    public ShoppingCart(List<Product> availableProducts) {
        this.availableProducts = availableProducts;
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public void addProduct(Product product, int quantity) {
        cart.put(product, cart.getOrDefault(product, 0) + quantity);
    }

    public void updateQuantity(Product product, int quantity) throws InvalidQuantityException {
        if (cart.containsKey(product)) {
            if (quantity > 0) {
                cart.put(product, quantity);
            } else {
                throw new InvalidQuantityException("Invalid quantity: " + quantity);
            }
        } else {
            throw new InvalidQuantityException("Product not found in the cart or not available.");
        }
    }

    public void removeProduct(Product product) {
        cart.remove(product);
    }

    public double calculateTotalBill() {
        double total = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            total += product.getPrice() * quantity;
        }
        if (discountStrategy != null) {
            total = discountStrategy.applyDiscount(total, cart.size());
        }
        return total;
    }

    public void displayAvailableProducts() {
        System.out.println("Available Products:");
        for (Product product : availableProducts) {
            if (product.isAvailable()) {
                System.out.println(product.getName() + " - Price: $" + product.getPrice());
            }
        }
    }

    public void displayCart() {
        System.out.println("Cart Items:");
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            System.out.println("You have " + quantity + " " + product.getName() + " in your cart.");
        }
        System.out.println("Total Bill: Your total bill is $" + calculateTotalBill() + ".");
    }
}

public class ECommerceCartSystem {
    private static final Logger logger = Logger.getLogger(ECommerceCartSystem.class.getName());

    public static void main(String[] args) {
        Product laptop = new Product("Laptop", 1000, true);
        Product headphones = new Product("Headphones", 50, true);

        List<Product> availableProducts = Arrays.asList(laptop, headphones);

        ShoppingCart cart = new ShoppingCart(availableProducts);
        cart.setDiscountStrategy(new PercentageDiscountStrategy(0.1)); // 10% discount

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Add product to cart");
            System.out.println("2. Update quantity");
            System.out.println("3. Remove product from cart");
            System.out.println("4. Display cart");
            System.out.println("5. Display available products");
            System.out.println("6. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

         switch (choice) {
         case 1 -> {
             cart.displayAvailableProducts();
             System.out.print("Enter product name: ");
             String productName = scanner.nextLine();
             System.out.print("Enter quantity: ");
             int quantity = scanner.nextInt();
             scanner.nextLine();
             
             try {
                 Product selectedProduct = findProductByName(productName, availableProducts);
                 if (selectedProduct != null) {
                     if (quantity <= 0) {
                         throw new InvalidQuantityException("Invalid quantity: " + quantity);
                     }
                     cart.addProduct(selectedProduct, quantity);
                     System.out.println("Added to cart.");
                     logger.log(Level.INFO, "Added {0} {1} to cart.", new Object[]{quantity, selectedProduct.getName()});
                 } else {
                     System.out.println("Product not found or not available.");
                 }
             } catch (ProductNotFoundException | InvalidQuantityException e) {
                 System.out.println(e.getMessage());
                 logger.warning(e.getMessage());
             }           }

   

                case 2 -> {
                    cart.displayCart();
                    System.out.print("Enter product name to update quantity: ");
                    String updateProductName = scanner.nextLine();
                    System.out.print("Enter new quantity: ");
                    int newQuantity = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character
                    try {
                        Product productToUpdate = findProductByName(updateProductName, availableProducts);
                        if (productToUpdate != null) {
                            cart.updateQuantity(productToUpdate, newQuantity);
                            System.out.println("Quantity updated.");
                            logger.log(Level.INFO, "Updated quantity of {0} to {1}", new Object[]{productToUpdate.getName(), newQuantity});
                        }
                    } catch (ProductNotFoundException | InvalidQuantityException e) {
                        System.out.println(e.getMessage());
                        logger.warning(e.getMessage());
                    }
                }
                case 3 -> {
                    cart.displayCart();
                    System.out.print("Enter product name to remove from cart: ");
                    String removeProductName = scanner.nextLine();
                    try {
                        Product productToRemove = findProductByName(removeProductName, availableProducts);
                        if (productToRemove != null) {
                            cart.removeProduct(productToRemove);
                            System.out.println("Product removed from cart.");
                            logger.log(Level.INFO, "Removed {0} from cart.", productToRemove.getName());
                        }
                    } catch (ProductNotFoundException e) {
                        System.out.println(e.getMessage());
                        logger.warning(e.getMessage());
                    }
                }
                case 4 -> cart.displayCart();
                case 5 -> cart.displayAvailableProducts();
                case 6 -> {
                    System.out.println("Thank you for shopping!");
                    return;
                }
                default -> {
                    System.out.println("Invalid choice. Please enter a valid option.");
                    logger.log(Level.WARNING, "Invalid choice: {0}", choice);
                }
            }
        }
    }

    private static Product findProductByName(String name, List<Product> products)
            throws ProductNotFoundException {
        Product selectedProduct = products.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name) && p.isAvailable())
                .findFirst()
                .orElse(null);
        if (selectedProduct == null) {
            throw new ProductNotFoundException("Product not found or not available: " + name);
        }
        return selectedProduct;
    }
}

class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

class InvalidQuantityException extends Exception {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
