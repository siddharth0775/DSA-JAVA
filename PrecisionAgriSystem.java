package demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class Node {
    int key;
    Map<String, Double> zoneData;
    Node left, right;
    int height;

    public Node(int key, Map<String, Double> zoneData) {
        this.key = key;
        this.zoneData = zoneData;
        this.height = 1;
    }
}

class AVLTree {
    private Node root;

    private int getHeight(Node node) {
        return (node == null) ? 0 : node.height;
    }

    private int getBalance(Node node) {
        return (node == null) ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    private Node rightRotate(Node y) {
        Node x = y.left;
        y.left = x.right;
        x.right = y;

        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

        return x;
    }

    private Node leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        y.left = x;

        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;

        return y;
    }

    private Node insert(Node node, int key, Map<String, Double> zoneData) {
        if (node == null) {
            return new Node(key, zoneData);
        }

        if (key < node.key) {
            node.left = insert(node.left, key, zoneData);
        } else if (key > node.key) {
            node.right = insert(node.right, key, zoneData);
        } else {
            return node;
        }

        node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;

        int balance = getBalance(node);

        // Left Left Case
        if (balance > 1 && key < node.left.key) {
            return rightRotate(node);
        }

        // Right Right Case
        if (balance < -1 && key > node.right.key) {
            return leftRotate(node);
        }

        // Left Right Case
        if (balance > 1 && key > node.left.key) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Left Case
        if (balance < -1 && key < node.right.key) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    public void insert(int key, Map<String, Double> zoneData) {
        root = insert(root, key, zoneData);
    }

    public void inorder(Node node) {
        if (node != null) {
            inorder(node.left);
            System.out.println("Zone ID: " + node.key + " Data: " + node.zoneData);
            inorder(node.right);
        }
    }

    public void printTree() {
        inorder(root);
    }

    public Map<String, Double> getZoneData(int key) {
        return search(root, key);
    }

    private Map<String, Double> search(Node node, int key) {
        if (node == null) {
            return null;
        }
        if (key == node.key) {
            return node.zoneData;
        } else if (key < node.key) {
            return search(node.left, key);
        } else {
            return search(node.right, key);
        }
    }
}

class CropRecommendation {
    private Map<String, Double[]> cropData = new HashMap<>();

    public void loadCropData(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String crop = data[0];
                Double[] params = new Double[6];
                for (int i = 1; i <= 6; i++) {
                    params[i - 1] = Double.parseDouble(data[i]);
                }
                cropData.put(crop, params);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRecommendation(Map<String, Double> zoneData) {
        if (zoneData == null) {
            return "Zone not found!";
        }

        double nitrogen = zoneData.getOrDefault("Nitrogen", 0.0);
        double phosphorus = zoneData.getOrDefault("Phosphorus", 0.0);
        double potassium = zoneData.getOrDefault("Potassium", 0.0);
        double temperature = zoneData.getOrDefault("Temperature", 0.0);
        double humidity = zoneData.getOrDefault("Humidity", 0.0);
        double ph = zoneData.getOrDefault("pH", 0.0);

        for (Map.Entry<String, Double[]> entry : cropData.entrySet()) {
            Double[] params = entry.getValue();

            if (Math.abs(nitrogen - params[0]) <= 10 &&
                Math.abs(phosphorus - params[1]) <= 10 &&
                Math.abs(potassium - params[2]) <= 10 &&
                Math.abs(temperature - params[3]) <= 5 &&
                Math.abs(humidity - params[4]) <= 10 &&
                Math.abs(ph - params[5]) <= 0.5) {
                return "Recommended Crop: " + entry.getKey();
            }
        }

        return "No suitable crop found!";
    }
}

public class PrecisionAgriSystem {
    public static void main(String[] args) {
        AVLTree tree = new AVLTree();
        CropRecommendation cropRec = new CropRecommendation();

        // Load urban data
        try (BufferedReader br = new BufferedReader(new FileReader("C:/Users/umasi/Documents/projects/DSA and Java/Cleaned_Urban_Areas.csv"))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                int zoneId = Integer.parseInt(data[0]);
                Map<String, Double> zoneData = new HashMap<>();
                zoneData.put("Latitude", Double.parseDouble(data[1]));
                zoneData.put("Longitude", Double.parseDouble(data[2]));
                zoneData.put("Area", Double.parseDouble(data[3]));
                zoneData.put("Nitrogen", Double.parseDouble(data[4]));
                zoneData.put("Phosphorus", Double.parseDouble(data[5]));
                zoneData.put("Potassium", Double.parseDouble(data[6]));
                zoneData.put("Temperature", Double.parseDouble(data[7]));
                zoneData.put("Humidity", Double.parseDouble(data[8]));
                zoneData.put("pH", Double.parseDouble(data[9]));

                tree.insert(zoneId, zoneData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load crop recommendation data
        cropRec.loadCropData("C:/Users/umasi/Documents/projects/DSA and Java/Crop_recommendation.csv");

        // Example: Get recommendation for zone 101
        int zoneId = 2000;
        Map<String, Double> zoneData = tree.getZoneData(zoneId);
        String recommendation = cropRec.getRecommendation(zoneData);

        System.out.println("Zone ID: " + zoneId);
        System.out.println(recommendation);
    }
}
