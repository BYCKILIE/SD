use diesel::PgConnection;
use diesel::r2d2::{ConnectionManager, PooledConnection};
use crate::db::pool::DbPool;

pub(crate) mod pool;

pub fn get_connection(pool: &DbPool) -> PooledConnection<ConnectionManager<PgConnection>> {
    pool.get().expect("Failed to get a connection from the pool.")
}