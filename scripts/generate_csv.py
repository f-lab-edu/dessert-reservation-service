#!/usr/bin/env python3
import csv
import random
from datetime import datetime, timedelta
from decimal import Decimal
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]
OUT_DIR = BASE_DIR / "shared-data" / "csv"

# -----------------------------
# Configuration (adjust as needed)
# -----------------------------
SEED = 42
USER_COUNT = 10_000
STORE_COUNT = 20
DESSERT_COUNT = 200
HOT_DESSERT_COUNT = 5
RESERVATION_COUNT = 50_000
SUBSCRIPTION_COUNT = 20_000

BASE_OPEN_DT = datetime(2026, 2, 25, 10, 0, 0)
PASSWORD_HASH = "$2a$10$XArr5HrCFpNaOUMTysE.Fu1x45e60msNCVehjFgI2iLmWo2iXAKzK"

random.seed(SEED)


def rand_dt_within(days_back=30):
    return datetime(2026, 2, 25, 12, 0, 0) - timedelta(
        days=random.randint(0, days_back),
        minutes=random.randint(0, 24 * 60 - 1),
    )


def fmt_dt(dt):
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def write_csv(path, header, rows):
    with path.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)


OUT_DIR.mkdir(parents=True, exist_ok=True)

# -----------------------------
# stores.csv
# -----------------------------
stores = []
for store_id in range(1, STORE_COUNT + 1):
    name = f"Store{store_id}"
    # Roughly Seoul-ish bounding box
    latitude = round(random.uniform(37.45, 37.65), 6)
    longitude = round(random.uniform(126.85, 127.15), 6)
    stores.append([store_id, name, latitude, longitude])

write_csv(
    OUT_DIR / "stores.csv",
    ["store_id", "name", "latitude", "longitude"],
    stores,
)

# -----------------------------
# users.csv
# -----------------------------
users = []
for user_id in range(1, USER_COUNT + 1):
    name = f"user{user_id}"
    email = f"user{user_id}@example.com"
    password = PASSWORD_HASH
    push_token = ""  # optional
    deleted_dt = "\\N"
    created_dt = fmt_dt(rand_dt_within(60))
    users.append([user_id, name, email, password, push_token, deleted_dt, created_dt])

write_csv(
    OUT_DIR / "users.csv",
    ["user_id", "name", "email", "password", "push_token", "deleted_dt", "created_dt"],
    users,
)

# -----------------------------
# desserts.csv
# -----------------------------
all_dessert_ids = list(range(1, DESSERT_COUNT + 1))
random.shuffle(all_dessert_ids)
hot_dessert_ids = set(all_dessert_ids[:HOT_DESSERT_COUNT])

desserts = []
dessert_price = {}
dessert_purchase_limit = {}

has_inventory_10 = False
for dessert_id in range(1, DESSERT_COUNT + 1):
    store_id = random.randint(1, STORE_COUNT)
    name = f"Dessert{dessert_id}"
    price = Decimal(random.randint(300, 3000)) / Decimal(100)  # 3.00 ~ 30.00

    if dessert_id in hot_dessert_ids:
        inventory = random.randint(10, 50)
        purchase_limit = 2
        open_dt = BASE_OPEN_DT
        open_status = "OPEN"
    else:
        inventory = random.randint(10, 100)
        purchase_limit = 5
        roll = random.random()
        if roll < 0.7:
            open_dt = BASE_OPEN_DT
            open_status = "OPEN"
        elif roll < 0.9:
            open_dt = BASE_OPEN_DT - timedelta(hours=random.randint(1, 48))
            open_status = "OPEN"
        else:
            open_dt = BASE_OPEN_DT + timedelta(hours=random.randint(1, 48))
            open_status = "PENDING"

    if inventory == 10:
        has_inventory_10 = True

    desserts.append([
        dessert_id,
        store_id,
        name,
        f"{price:.2f}",
        inventory,
        purchase_limit,
        fmt_dt(open_dt),
        open_status,
    ])
    dessert_price[dessert_id] = price
    dessert_purchase_limit[dessert_id] = purchase_limit

# Ensure at least one dessert has inventory = 10
if not has_inventory_10 and desserts:
    desserts[0][4] = 10

write_csv(
    OUT_DIR / "desserts.csv",
    [
        "dessert_id",
        "store_id",
        "name",
        "price",
        "inventory",
        "purchase_limit",
        "open_dt",
        "open_status",
    ],
    desserts,
)

# -----------------------------
# reservations.csv
# -----------------------------
reservations = []

heavy_user_count = max(1, USER_COUNT // 100)  # top 1%
heavy_users = list(range(1, heavy_user_count + 1))
other_users = list(range(heavy_user_count + 1, USER_COUNT + 1))

hot_list = list(hot_dessert_ids)
non_hot_list = [d for d in range(1, DESSERT_COUNT + 1) if d not in hot_dessert_ids]

# Weighted hot distribution: top1 gets 50% of hot traffic
hot_weights = [0.5] + [0.5 / (len(hot_list) - 1)] * (len(hot_list) - 1)

# Track per (user, dessert, date) counts to stay within purchase_limit
purchase_tracker = {}

for reservation_id in range(1, RESERVATION_COUNT + 1):
    # user selection
    if random.random() < 0.4:
        user_id = random.choice(heavy_users)
    else:
        user_id = random.choice(other_users)

    # dessert selection
    if random.random() < 0.8:
        dessert_id = random.choices(hot_list, weights=hot_weights, k=1)[0]
    else:
        dessert_id = random.choice(non_hot_list)

    # created_dt and day bucket
    created_dt = rand_dt_within(14)
    day_key = created_dt.strftime("%Y-%m-%d")
    key = (user_id, dessert_id, day_key)

    current = purchase_tracker.get(key, 0)
    limit = dessert_purchase_limit[dessert_id]
    if current >= limit:
        # try a different dessert once to keep within limits
        dessert_id = random.choice(non_hot_list)
        key = (user_id, dessert_id, day_key)
        current = purchase_tracker.get(key, 0)
        limit = dessert_purchase_limit[dessert_id]

    count = 1
    purchase_tracker[key] = current + count

    total_price = dessert_price[dessert_id] * Decimal(count)

    roll = random.random()
    if roll < 0.8:
        reserve_status = "CONFIRMED"
    elif roll < 0.95:
        reserve_status = "PENDING"
    else:
        reserve_status = "CANCELLED"

    reservations.append([
        reservation_id,
        user_id,
        dessert_id,
        count,
        f"{total_price:.2f}",
        reserve_status,
        fmt_dt(created_dt),
    ])

write_csv(
    OUT_DIR / "reservations.csv",
    [
        "reservation_id",
        "user_id",
        "dessert_id",
        "count",
        "total_price",
        "reserve_status",
        "created_dt",
    ],
    reservations,
)

# -----------------------------
# subscriptions.csv
# -----------------------------
subscriptions = []
seen = set()
while len(subscriptions) < SUBSCRIPTION_COUNT:
    user_id = random.randint(1, USER_COUNT)
    store_id = random.randint(1, STORE_COUNT)
    key = (user_id, store_id)
    if key in seen:
        continue
    seen.add(key)
    deleted_dt = "\\N"
    created_dt = fmt_dt(rand_dt_within(60))
    subscriptions.append([user_id, store_id, deleted_dt, created_dt])

write_csv(
    OUT_DIR / "subscriptions.csv",
    ["user_id", "store_id", "deleted_dt", "created_dt"],
    subscriptions,
)

# -----------------------------
# notification_template.csv
# -----------------------------
notification_templates = [
    ["DESSERT_UPLOAD", "디저트 등록", "신규 디저트가 등록되었습니다.", "DESSERT", "https://example.com/desserts", fmt_dt(rand_dt_within(30))],
    ["OPEN_NOTI", "오픈 알림", "디저트 오픈이 임박했습니다.", "DESSERT", "https://example.com/open", fmt_dt(rand_dt_within(30))],
]

write_csv(
    OUT_DIR / "notification_template.csv",
    ["template_key", "title", "body", "noti_type", "url", "created_dt"],
    notification_templates,
)

print(f"CSV files written to {OUT_DIR}")
