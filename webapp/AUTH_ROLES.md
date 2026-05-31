# Admin and Customer Login

The application has two login portals:

- Customer: `/customer/login` and `/customer/dashboard`
- Admin: `/admin/login` and `/admin/dashboard`

Public registration always creates a `CUSTOMER` account. Passwords are stored as BCrypt hashes through `BCryptPasswordEncoder`; raw passwords are never inserted into the database.

## Create The First Admin

1. Register a normal customer account through `/register`.
2. Promote that chosen account in MySQL:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE username = 'your_admin_username';
```

The password hash remains unchanged, so that account can then log in through `/admin/login` using its existing password.

## Permissions

- Customers can access upcoming concert and ticket-availability views in their customer dashboard.
- Admins can access operational statistics, customer counts and seat inventory per concert in their admin dashboard.
- A correctly authenticated account is rejected when used on the portal for the other role.
