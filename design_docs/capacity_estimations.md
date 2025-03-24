# Capacity Estimations for Popcorn Palace

## Capacity Estimations - Summary
- Theaters: ~400
- Seats per Theater: ~100
- Showtimes: ~5
- Daily Bookings (writes): ~36K
- Safety buffer: +25%
- 5:1 read:write relation
- Active hours: ~12 hours
- Traffic:
  - Bandwith Usage: ~31 KB/second during peak
  - Peak writes/second: ~4 writes/second
  - Peak reads/second: ~20 reads/second

- Storage: ~1GB per year (Assuming cleanup of showtimes one a week and cleanup of bookings once a year)

## Core Assumptions

| Parameter | Value | Notes |
|-----------|-------|-------|
| Average seats per showtime | 100 | Based on typical theater capacity |
| Number of theaters | 400 | Across all locations |
| Average movie duration | 140 minutes | Not including commercials |
| Average showtime duration | 170 minutes | Including commercials/previews |
| Operating hours | 12:00 - 00:00 | 00:00 as the latest start time for a movie |
| Showtimes per theater | 5 per day | Based on duration and operating hours |

## Traffic Calculations

### Daily Traffic
- **Annual ticket sales**: 13,000,000 tickets
- **Daily bookings**: 13,000,000 ÷ 365 = 35,616 ≈ 36,000 bookings per day
- **Read:Write ratio**: 5:1 (5 showtime checks per booking)
- **Daily read operations**: 36,000 × 5 = 180,000 reads

### Operations Per Second (OPS)
- **Average write operations**: 36,000 ÷ (12 hours × 3,600 seconds) ≈ 0.83 writes/second
- **Average read operations**: 180,000 ÷ (12 hours × 3,600 seconds) ≈ 4.17 reads/second
- **Total average OPS**: 5 operations/second

### Safety Buffer
To account for variations, inaccuracies, and unexpected demand spikes, we apply a 25% safety buffer to all capacity estimations.

## Peak Load Considerations
Peak load estimation is critical for ensuring system stability during high-traffic periods:

### Peak Load Estimation
- **Industry patterns**: Movie theaters typically experience highest traffic during evenings and weekends
- **Distribution assumptions**: Based on typical movie theater attendance patterns:
  - Weekends (Fri-Sun): ~60% of weekly traffic
  - Weekday evenings (Mon-Thu, 18:00-22:00): ~30% of weekly traffic  
  - Weekday daytime: ~10% of weekly traffic

### Peak Calculations
- **Busiest time slot**: Friday and Saturday evenings (18:00-22:00)
- **Estimated peak ratio**: 4-5× the average hourly rate
- **Average OPS**: 5 operations/second
- **Peak OPS**: 20-25 operations/second (during busiest periods)
- **Peak writes/second**: ~4 writes/second
- **Peak reads/second**: ~20 reads/second

*Note: These estimates should be validated with actual usage data once the system is operational.*

## Storage Calculations

Based on the data models defined in the system design document, we can estimate storage requirements:

### Entity Size Estimations

| Entity | Fields | Approximate Size | Notes |
|--------|--------|------------------|-------|
| Movie | UUID (16 bytes) + title (100 bytes) + genre (20 bytes) + duration (4 bytes) + rating (8 bytes) + releaseYear (4 bytes) | ~152 bytes | Assuming average title length of 50 characters |
| Showtime | UUID (16 bytes) + movieId (16 bytes) + price (8 bytes) + theater (50 bytes) + startTime (8 bytes) + endTime (8 bytes) | ~106 bytes | |
| Booking | UUID (16 bytes) + showtimeId (16 bytes) + seatNumber (4 bytes) + userId (16 bytes) | ~52 bytes | |
| Theater | UUID (16 bytes) + name (50 bytes) + numberOfSeats (4 bytes) | ~70 bytes | |
| ShowSeat | UUID (16 bytes) + showtimeId (16 bytes) + seatId (16 bytes) + isAvailable (1 byte) | ~49 bytes | |
| Seat | UUID (16 bytes) + theaterId (16 bytes) + number (4 bytes) | ~36 bytes | |

### Total Storage Requirements

| Entity | Count | Size per Entity | Total Size |
|--------|-------|-----------------|------------|
| Movies | 1,000 (estimate) | 152 bytes | 152 KB |
| Showtimes | 400 theaters × 5 showtimes × 7 days = 14,000 | 106 bytes | 1.48 MB |
| Bookings | 13M per year = ~36K per day | 52 bytes | 1.87 MB per day, ~683 MB per year |
| Theaters | 400 | 70 bytes | 28 KB |
| ShowSeats | 14,000 showtimes × 100 seats = 1.4M | 49 bytes | 68.6 MB |
| Seats | 400 theaters × 100 seats = 40,000 | 36 bytes | 1.44 MB |

Note: Assumptions of showtimes that are cleaned once a week, bookings that are cleaned one a year.

**Total Base Storage**: ~755 MB per year

With 25% buffer: ~944 MB per year = ~1GB per year

## Network Bandwidth Calculations

### API Traffic Estimation

| Operation Type | Payload Size (estimate) | Daily Operations | Daily Bandwidth | Peak Bandwidth/Second |
|----------------|-------------------------|------------------|-----------------|----------------------|
| GET requests | 1 KB response (avg) | 180,000 reads | 180 MB | ~20 KB/s |
| POST/PUT requests | 0.5 KB request + 0.5 KB response | 36,000 writes | 36 MB | ~4 KB/s |

**Total Daily Bandwidth**: ~216 MB <br>
**Average Bandwidth**: ~5 KB/second <br>
**Peak Bandwidth**: ~25 KB/second (during busiest periods)

With 25% buffer: ~31 KB/second during peak

## Resource Allocation Summary

| Resource | Average Requirement | Peak Requirement |
|----------|---------------------|------------------|
| Storage | 1 GB per year | N/A |
| Bandwidth | 5 KB/second | 31 KB/second |

*Note: All calculations include the 25% safety buffer mentioned earlier.*

## Data Sources

1. [Cinema City](https://www.cinema-city.co.il) - Average seats number, average closing times
2. [Globes](https://www.globes.co.il/news/article.aspx?did=1001276780) - Average number of theaters
3. [13TV](https://13tv.co.il/item/entertainment/cinema/dtfqc-904356906/) - Average movie duration
4. [Planet Cinema FAQ](https://www.planetcinema.co.il/static/iw/il/FAQ) - Operating hours of theaters
5. [Calcalist](https://www.calcalist.co.il/style/article/rywiyvglke) - Annual ticket sales
