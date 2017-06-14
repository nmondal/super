SELECT users.name, posts.id, posts.title
FROM posts
JOIN users ON users.id = posts.userId ;
