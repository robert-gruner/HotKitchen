package hotkitchen.common

class CategoryDoesNotExistException(msg: String = "Category does not exist"): NoSuchElementException(msg)
class MealDoesNotExistException(msg: String = "Meal does not exist"): NoSuchElementException(msg)
class OrderDoesNotExistException(msg: String = "Order does not exist"): NoSuchElementException(msg)
class ProfileDoesNotExistException(msg: String = "Profile does not exist"): NoSuchElementException(msg)
class ProfileEmptyException(msg: String = "Profile is empty"): IllegalStateException(msg)
class UserAlreadyExistsException(msg: String = "User already exists"): IllegalStateException(msg)
class UserDoesNotExistException(msg: String = "User does not exist"): NoSuchElementException(msg)
class WrongPasswordException(msg: String = "Wrong password"): IllegalArgumentException(msg)
