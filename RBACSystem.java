import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

enum Resource {
    BATCH,
    POLICY,
    RULE
}

enum Action {
    READ,
    EDIT,
    DELETE
}

enum RoleName {
    ADMIN,
    EDITOR,
    READER
}

enum HTTPMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE
}

class User {
    String id;
    String name;
    String email;
    String mobile;
    String password;

    public User(String name, String email, String mobile, String password) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }
}

class Role {
    String id;
    RoleName name;
    String description;

    public Role(RoleName name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public RoleName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

class Permission {
    String id;
    Resource resource;
    Action action;
    String description;

    public Permission(Resource resource, Action action, String description) {
        this.id = UUID.randomUUID().toString();
        this.resource = resource;
        this.action = action;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public Action getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }
}

class UserRole {
    User user;
    Role role;

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }
}

class RolePermission {
    Role role;
    Permission permission;

    public RolePermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    public Role getRole() {
        return role;
    }

    public Permission getPermission() {
        return permission;
    }
}

class Request {
    String id;
    String apiUrl;
    HTTPMethod httpMethod;
    User user;

    public Request(String apiUrl, HTTPMethod httpMethod, User user) {
        this.id = UUID.randomUUID().toString();
        this.apiUrl = apiUrl;
        this.httpMethod = httpMethod;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public HTTPMethod getHttpMethod() {
        return httpMethod;
    }

    public User getUser() {
        return user;
    }
}

public class RBACSystem {

    User user1 = new User("name1", "email1@abc.com", "+919999999999", "pass1");
    User user2 = new User("name2", "email2@abc.com", "+919999999990", "pass2");
    User user3 = new User("name3", "email3@abc.com", "+919999999991", "pass3");

    Role role1 = new Role(RoleName.ADMIN, "admin role");
    Role role2 = new Role(RoleName.EDITOR, "editor role");
    Role role3 = new Role(RoleName.READER, "reader role");

    Permission permission1 = new Permission(Resource.BATCH, Action.READ, "batch read");
    Permission permission2 = new Permission(Resource.BATCH, Action.EDIT, "batch edit");
    Permission permission3 = new Permission(Resource.BATCH, Action.DELETE, "batch delete");

    UserRole userRole1 = new UserRole(user1, role1);
    UserRole userRole2 = new UserRole(user2, role2);
    UserRole userRole3 = new UserRole(user3, role3);
    List<UserRole> userRoles = new ArrayList<>(Arrays.asList(userRole1, userRole2, userRole3));

    RolePermission rolePermission1 = new RolePermission(role1, permission1);
    RolePermission rolePermission2 = new RolePermission(role1, permission2);
    RolePermission rolePermission3 = new RolePermission(role1, permission3);
    RolePermission rolePermission4 = new RolePermission(role2, permission1);
    RolePermission rolePermission5 = new RolePermission(role2, permission2);
    RolePermission rolePermission6 = new RolePermission(role3, permission1);
    List<RolePermission> rolePermissions = new ArrayList<>(Arrays.asList(rolePermission1, rolePermission2, rolePermission3, rolePermission4, rolePermission5, rolePermission6));

    public boolean authorizeRequest(Request request) {
        List<UserRole> roles = userRoles.stream().filter(u -> u.getUser().equals(request.getUser())).toList();
        List<RolePermission> permissions = new ArrayList<>();
        for (UserRole userRole : roles) {
            permissions.addAll(rolePermissions.stream().filter(p -> p.getRole().equals(userRole.getRole())).toList());
        }

        Action action;
        if (request.getHttpMethod().equals(HTTPMethod.GET)){
            action = Action.READ;
        } else if (request.getHttpMethod().equals(HTTPMethod.POST)){
            action = Action.EDIT;
        } else if (request.getHttpMethod().equals(HTTPMethod.PUT)){
            action = Action.EDIT;
        } else if (request.getHttpMethod().equals(HTTPMethod.PATCH)){
            action = Action.EDIT;
        } else if (request.getHttpMethod().equals(HTTPMethod.DELETE)){
            action = Action.DELETE;
        } else {
            action = null;
        }

        Resource resource;
        String[] parts = request.getApiUrl().split("/");
        String resourceStr = (parts.length > 2) ? parts[2] : null;
        assert resourceStr != null;
        resource = switch (resourceStr) {
            case "batches" -> Resource.BATCH;
            case "policies" -> Resource.POLICY;
            case "rules" -> Resource.RULE;
            default -> null;
        };

        List<Permission> permissionList = permissions.stream().map(RolePermission::getPermission).filter(p -> p.getAction().equals(action) && p.getResource().equals(resource)).toList();
        return !permissionList.isEmpty();
    }

    public static void main(String[] args) {
        RBACSystem o = new RBACSystem();
        Request request = new Request("/api/batches/123", HTTPMethod.GET, o.user1);
        System.out.println(o.authorizeRequest(request));
    }
}
