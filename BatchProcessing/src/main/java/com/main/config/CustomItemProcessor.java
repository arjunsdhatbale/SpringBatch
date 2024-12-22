package com.main.config;

import org.springframework.batch.item.ItemProcessor;

import com.main.model.Product;

public class CustomItemProcessor implements ItemProcessor<Product, Product> {

	@Override
    public Product process(Product item) throws Exception {
        
        // Ensure the discount and price are not null or empty
        if (item.getDiscount() != null && !item.getDiscount().trim().isEmpty() && 
            item.getPrice() != null && !item.getPrice().trim().isEmpty()) {

            // Calculate discounted price 
            try {
                // Convert discount to a percentage and parse price and discount values as doubles
                double discountPer = Double.parseDouble(item.getDiscount().trim()); 
                double originalPrice = Double.parseDouble(item.getPrice().trim());
                
                // Calculate the discount amount
                double discountAmount = (discountPer / 100) * originalPrice;
                
                // Calculate the final price after discount
                double finalPrice = originalPrice - discountAmount; 

                // Set the discounted price in the Product object
                item.setDiscountedPrice(finalPrice);  // Assuming discountedPrice is of type double
                
            } catch (NumberFormatException e) {
                // Log or handle the exception if the discount or price is not a valid number
                throw new Exception("Invalid discount or price value: " + e.getMessage(), e);
            }
        } else {
            // If discount or price is missing, handle accordingly (maybe log a warning)
            throw new Exception("Discount or price is missing for product: " + item.getTitle());
        }

        return item;  // Return the updated product with the discounted price
    }
	
	
//	@Override
//	public Product process(Product item) throws Exception {
//		 
//		if(item.getDiscount() != null && item.getDiscount().trim().isEmpty() &&
//				item.getPrice()!= null && !item.getPrice().trim().isEmpty()   ) {
//			try {
//				double discountPer = Double.parseDouble(item.getDiscount().trim()); 
//				double originalPrice = Double.parseDouble(item.getPrice().trim()); 
//				
//				double discountAmount = (discountPer / 100) * originalPrice; 
//				
//				double finalPrice = originalPrice - discountAmount;
//				
//				item.setDiscountedPrice(finalPrice);
//			}catch (NumberFormatException e) {
// 
//				throw new Exception("Invalid discount or price value  " + e.getMessage(), e); 
//			}
//		}else {
//			throw new Exception("Discount or price is missing for product : " + item.getTitle()); 
//		}
//		
//		// calculate discounted price 
//		
////		int discountPer = Integer.parseInt(item.getDiscount().trim()); 
////		
////	    double originalPrice = Double.parseDouble(item.getPrice());
////	    
////		double discount =  (discountPer/100) * originalPrice;
////		
////		double finalPrice = originalPrice - discount; 
////		item.setDiscountedPrice(String.valueOf(finalPrice));
//		
//		return item;
//	}

}
