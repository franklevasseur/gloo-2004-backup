package utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircularStackTest {

//    @BeforeEach
//    void setUp() {
//    }
//
//    @AfterEach
//    void tearDown() {
//    }

    @Test
    void givenStackOfLengthThree_whenAddingFiveElements_thenOnlyLastThreeElementsAreLeft() {
        CircularStack<Integer> stack = new CircularStack<Integer>(3);

        stack.add(1);
        stack.add(2);
        stack.add(3);
        stack.add(4);
        stack.add(5);

        int el1 = stack.pop();
        int el2 = stack.pop();
        int el3 = stack.pop();

        assertEquals(el1, 5);
        assertEquals(el2, 4);
        assertEquals(el3, 3);
    }

    @Test
    void givenStackOfLengthThree_whenStackIsEmpty_thenPopShouldReturnNull() {
        CircularStack<Integer> stack = new CircularStack<Integer>(3);

        stack.add(1);
        stack.add(2);
        stack.add(3);
        stack.add(4);
        stack.add(5);

        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();
        stack.pop();

        Integer remaining = stack.pop();

        assertNull(remaining);
    }
}