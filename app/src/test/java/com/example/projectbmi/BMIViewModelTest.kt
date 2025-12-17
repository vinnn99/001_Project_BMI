package com.example.projectbmi

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BMI calculation logic
 * 
 * BMI Formula: BMI = weight (kg) / (height (m) * height (m))
 * 
 * @author CI/CD Pipeline Test
 */
class BMIViewModelTest {

    private lateinit var viewModel: BMIViewModel

    @org.junit.Before
    fun setUp() {
        viewModel = BMIViewModel()
    }

    /**
     * Test Case 1: Normal BMI Calculation
     * Input: weight = 70.0 kg, height = 175 cm
     * Expected: BMI = 22.9 (rounded to 1 decimal)
     * Category: Normal
     */
    @Test
    fun testBMICalculation_Normal() {
        // Arrange
        viewModel.setWeight(70.0f)
        viewModel.setHeight(175)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        val expectedBmi = 22.857142857f  // 70 / (1.75 * 1.75)
        assertEquals("BMI should be approximately 22.9", 22.9f, bmi, 0.1f)
        assertEquals("Category should be Normal", "Normal", category)
    }

    /**
     * Test Case 2: Underweight BMI Calculation
     * Input: weight = 50.0 kg, height = 170 cm
     * Expected: BMI < 18.5
     * Category: Underweight
     */
    @Test
    fun testBMICalculation_Underweight() {
        // Arrange
        viewModel.setWeight(50.0f)
        viewModel.setHeight(170)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        assertTrue("BMI should be less than 18.5", bmi < 18.5f)
        assertEquals("Category should be Underweight", "Underweight", category)
    }

    /**
     * Test Case 3: Overweight BMI Calculation
     * Input: weight = 85.0 kg, height = 170 cm
     * Expected: 25.0 <= BMI < 30.0
     * Category: Overweight
     */
    @Test
    fun testBMICalculation_Overweight() {
        // Arrange
        viewModel.setWeight(85.0f)
        viewModel.setHeight(170)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        assertTrue("BMI should be between 25 and 30", bmi in 25f..30f)
        assertEquals("Category should be Overweight", "Overweight", category)
    }

    /**
     * Test Case 4: Obese BMI Calculation
     * Input: weight = 100.0 kg, height = 170 cm
     * Expected: BMI >= 30.0
     * Category: Obese
     */
    @Test
    fun testBMICalculation_Obese() {
        // Arrange
        viewModel.setWeight(100.0f)
        viewModel.setHeight(170)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        assertTrue("BMI should be greater than or equal to 30", bmi >= 30f)
        assertEquals("Category should be Obese", "Obese", category)
    }

    /**
     * Test Case 5: Edge Case - Zero Height
     * Input: weight = 70.0 kg, height = 0 cm
     * Expected: BMI = 0.0 (safety check to prevent division by zero)
     */
    @Test
    fun testBMICalculation_ZeroHeight() {
        // Arrange
        viewModel.setWeight(70.0f)
        viewModel.setHeight(0)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        assertEquals("BMI should be 0 when height is 0", 0.0f, bmi)
    }

    /**
     * Test Case 6: High Values (Extreme Case)
     * Input: weight = 150.0 kg, height = 180 cm
     * Expected: BMI = 46.3 (approx)
     * Category: Obese
     */
    @Test
    fun testBMICalculation_HighValues() {
        // Arrange
        viewModel.setWeight(150.0f)
        viewModel.setHeight(180)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        val expectedBmi = 46.3f  // 150 / (1.80 * 1.80)
        assertEquals("BMI should be approximately 46.3", expectedBmi, bmi, 0.1f)
        assertEquals("Category should be Obese", "Obese", category)
    }

    /**
     * Test Case 7: Low Values (Extreme Case)
     * Input: weight = 40.0 kg, height = 160 cm
     * Expected: BMI = 15.6 (approx)
     * Category: Underweight
     */
    @Test
    fun testBMICalculation_LowValues() {
        // Arrange
        viewModel.setWeight(40.0f)
        viewModel.setHeight(160)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        val expectedBmi = 15.625f  // 40 / (1.60 * 1.60)
        assertEquals("BMI should be approximately 15.6", expectedBmi, bmi, 0.1f)
        assertEquals("Category should be Underweight", "Underweight", category)
    }

    /**
     * Test Case 8: Boundary Case - Exactly Normal (BMI = 18.5)
     * Input: weight = 56.65625 kg, height = 175 cm
     * Expected: BMI = 18.5 (exact boundary)
     * Category: Normal (not underweight) - first condition is bmi < 18.5
     */
    @Test
    fun testBMICalculation_BoundaryNormal() {
        // Arrange - Calculate weight for exact BMI = 18.5
        // At BMI = 18.5, height = 1.75m: weight = 18.5 * 1.75 * 1.75 = 56.65625
        viewModel.setWeight(56.65625f)
        viewModel.setHeight(175)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        assertEquals("BMI should be exactly 18.5", 18.5f, bmi, 0.001f)
        assertEquals("Category should be Normal", "Normal", category)
    }

    /**
     * Test Case 9: Boundary Case - Exactly Overweight (BMI = 25.0)
     * Input: weight = 76.5625 kg, height = 175 cm
     * Expected: BMI = 25.0 (exact boundary)
     * Category: Overweight - second condition is bmi < 25
     */
    @Test
    fun testBMICalculation_BoundaryOverweight() {
        // Arrange - Calculate weight for exact BMI = 25.0
        // At BMI = 25.0, height = 1.75m: weight = 25.0 * 1.75 * 1.75 = 76.5625
        viewModel.setWeight(76.5625f)
        viewModel.setHeight(175)
        
        // Act
        val (bmi, category) = viewModel.calculateBmi()
        
        // Assert
        assertEquals("BMI should be exactly 25.0", 25.0f, bmi, 0.001f)
        assertEquals("Category should be Overweight", "Overweight", category)
    }

    /**
     * Test Case 10: Multiple Calculations in Sequence
     * Verify that changing values correctly updates BMI
     */
    @Test
    fun testBMICalculation_MultipleCalculations() {
        // First calculation
        viewModel.setWeight(70.0f)
        viewModel.setHeight(175)
        val (bmi1, category1) = viewModel.calculateBmi()
        assertEquals("First category should be Normal", "Normal", category1)
        
        // Change to overweight
        viewModel.setWeight(90.0f)
        val (bmi2, category2) = viewModel.calculateBmi()
        assertEquals("Second category should be Overweight", "Overweight", category2)
        
        // Verify BMI2 > BMI1
        assertTrue("Second BMI should be greater than first", bmi2 > bmi1)
    }
}
