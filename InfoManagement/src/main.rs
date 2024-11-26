use actix_web::{web, App, HttpServer};
use dotenv::dotenv;
use crate::routes::init_routes;

mod routes;
mod models;
mod services;
mod utils;

mod schema;
mod db;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    dotenv().ok();

    let pool = db::pool::create_pool();

    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(pool.clone()))
            .configure(init_routes)
    })
        .bind("localhost:7023")?
        .run()
        .await
}
