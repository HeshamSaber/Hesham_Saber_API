package org.hsqa.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PetCrudTests {
    
    static {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";
    }
    
    static class Pet {
        public Integer id;
        public Category category;
        public String name;
        public String[] photoUrls;
        public Tag[] tags;
        public String status;
        
        public Pet(Integer id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
        
        static class Category {
            public Integer id;
            public String name;
            
            public Category(Integer id, String name) {
                this.id = id;
                this.name = name;
            }
        }
        
        static class Tag {
            public Integer id;
            public String name;
            
            public Tag(Integer id, String name) {
                this.id = id;
                this.name = name;
            }
        }
    }
    
    // create pet => positive
    @Test(priority = 1)
    public void createPet() {
        Pet pet = new Pet(123, "dog", "available");
        pet.category = new Pet.Category(1, "Dogs");
        pet.photoUrls = new String[]{"http://example.com/photo.jpg"};
        pet.tags = new Pet.Tag[]{new Pet.Tag(1, "friendly")};
        
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(pet)
            .post("/pet");
            
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getInt("id"), 123);
        Assert.assertEquals(response.jsonPath().getString("name"), "dog");
        Assert.assertEquals(response.jsonPath().getString("status"), "available");
        Assert.assertEquals(response.jsonPath().getInt("category.id"), 1);
        Assert.assertEquals(response.jsonPath().getString("category.name"), "Dogs");
        Assert.assertEquals(response.jsonPath().getString("photoUrls[0]"), "http://example.com/photo.jpg");
        Assert.assertEquals(response.jsonPath().getInt("tags[0].id"), 1);
        Assert.assertEquals(response.jsonPath().getString("tags[0].name"), "friendly");
    }
    
    // create pet => negative
    @Test(priority = 2)
    public void createPetInvalidID() {
        String invalidPet = "{\"id\": \"invalid\", \"name\": \"dog\", \"status\": \"available\"}";

        Response response = RestAssured.given()
            .contentType("application/json")
            .body(invalidPet)
            .post("/pet");
            
        Assert.assertEquals(response.getStatusCode(), 500);
        Assert.assertEquals(response.jsonPath().getInt("code"), 500);
        Assert.assertEquals(response.jsonPath().getString("type"), "unknown");
        Assert.assertEquals(response.jsonPath().getString("message"), "something bad happened");
    }
    
    // get pet => positive
    @Test(priority = 3)
    public void getPet() {
        Response response = RestAssured.get("/pet/123");
        
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getInt("id"), 123);
        Assert.assertNotNull(response.jsonPath().getString("name"));
    }
    
    // get pet => negative
    @Test(priority = 4)
    public void getPetInvalidID() {
        Response response = RestAssured.get("/pet/invalid");

        Assert.assertEquals(response.getStatusCode(), 404);
        Assert.assertEquals(response.jsonPath().getInt("code"), 404);
        Assert.assertEquals(response.jsonPath().getString("type"), "unknown");
        Assert.assertEquals(response.jsonPath().getString("message"), "java.lang.NumberFormatException: For input string: \"invalid\"");
    }
    
    // update pet => positive
    @Test(priority = 5)
    public void updatePet() {
        Pet pet = new Pet(123, "doggie", "sold");
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(pet)
            .post("/pet");

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getInt("id"), 123);
        Assert.assertEquals(response.jsonPath().getString("name"), "doggie");
        Assert.assertEquals(response.jsonPath().getString("status"), "sold");
    }
    
    // update pet => negative
    @Test(priority = 6)
    public void updatePetInvalidID() {
        String invalidPet = "{\"id\": \"invalid\", \"name\": \"dog\", \"status\": \"available\"}";
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(invalidPet)
            .put("/pet");
            
        Assert.assertEquals(response.getStatusCode(), 500);
        Assert.assertEquals(response.jsonPath().getInt("code"), 500);
        Assert.assertEquals(response.jsonPath().getString("type"), "unknown");
        Assert.assertEquals(response.jsonPath().getString("message"), "something bad happened");
    }
    
    // delete pet => positive
    @Test(priority = 7)
    public void deletePet() {
        Response response = RestAssured.delete("/pet/123");
        
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getInt("code"), 200);
        Assert.assertEquals(response.jsonPath().getString("type"), "unknown");
        Assert.assertEquals(response.jsonPath().getString("message"), "123");
    }
    // delete pet => negative
    @Test(priority = 8)
    public void deletePetInvalidID() {
        Response response = RestAssured.delete("/pet/1234567");

        response.then().log().all();
        Assert.assertEquals(response.getStatusCode(), 404);
    }
    // bad request => negative
    @Test(priority = 9)
    public void badRequest() {
        Response response = RestAssured.given()
            .contentType("application/json")
            .body("bad json")
            .post("/pet");
            
        Assert.assertEquals(response.getStatusCode(), 400);
    }
}