package customer.api;

// tag::class[]
public record Customer(String customerId, String email, String name, Address address) {

  public Customer withName(String newName){
    return new Customer(customerId, email, newName, address);
  }

  public Customer withAddress(Address newAddress){
    return new Customer(customerId, email, name, newAddress);
  }
}
// end::class[]
