package com.example.demo.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController
{
    private final UserRepository repository;

    public UserController(UserRepository repository)
    {
        this.repository = repository;
    }

    @GetMapping("")
    public Iterable<User> readAll() { return this.repository.findAll(); }

    @PostMapping("")
    public User create(@RequestBody User newUser) { return this.repository.save(newUser); }

    @GetMapping("/{id}")
    public Object readById(@PathVariable Long id)
    {
        return (this.repository.existsById(id)) ?
                this.repository.findById(id) :
                "This entry does not exist" ;
    }

    @PatchMapping("/{id}")
    public User updateOrCreate(@PathVariable Long id,
                               @RequestBody Map<String, Object> fields)
    {

        if (! repository.existsById(id)) // not in the repo -- create POJO and add it
        {
            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
            final User newUser = mapper.convertValue(fields, User.class);
            return this.repository.save(newUser);
        }

        User myUser = this.repository.findById(id).orElse(null);

        fields.forEach((k, v) -> {
            // use reflection to get field k on object and set it to value v
            // Change Claim.class to whatver your object is: Object.class
            Field field = ReflectionUtils.findField(User.class, k); // find field in the object class
            field.setAccessible(true);
            ReflectionUtils.setField(field, myUser, v); // set given field for defined object to value V
        });

        return this.repository.save(myUser);
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id)
    {
        if (this.repository.existsById(id))
        {
            this.repository.deleteById(id);
            return Map.of("count", this.repository.count());
        }
        else return "There is no object at this ID";
    }

    @PostMapping("/authenticate")
    public Object checkPassword(@RequestBody Map<String, String> fields)
    {
        List<User> matches = this.repository.findByEmail(fields.get("email"));

        for (User match : matches)
        {
            if (match.getPassword().equals(fields.get("password")))
            {
                return Map.of("authenticated", true,
                        "user", match);
            }
        }

        return Map.of("authenticated", false);
    }

}
