# Raspberry Pi Deployment Checklist

Complete guide for deploying StoLat on a Raspberry Pi in your local network.

---

## Phase 1: Raspberry Pi Setup

### Install Ubuntu Server

- [ ] Download Ubuntu Server 24.04 LTS (64-bit) for Raspberry Pi from
  https://ubuntu.com/download/raspberry-pi
- [ ] Flash the image to a microSD card using Raspberry Pi Imager
  - Set hostname (e.g., `stolat`)
  - Enable SSH
  - Configure Wi-Fi (or use Ethernet)
  - Set username/password
- [ ] Insert the microSD card and boot the Pi
- [ ] SSH into the Pi: `ssh your-user@stolat.local`
- [ ] Update the system: `sudo apt update && sudo apt upgrade -y`

### Install Docker

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# Log out and back in for group change to take effect
```

- [ ] Verify: `docker --version`
- [ ] Verify: `docker compose version`

### Mount your music drive

If your music collection is on an external drive or NAS:

```bash
# Example: mount a USB drive
sudo mkdir -p /mnt/music
sudo mount /dev/sda1 /mnt/music

# Make it permanent (add to /etc/fstab)
echo '/dev/sda1 /mnt/music ext4 defaults,nofail 0 2' | sudo tee -a /etc/fstab
```

Or for an NFS share (e.g., from a NAS):

```bash
sudo apt install nfs-common -y
sudo mkdir -p /mnt/music
sudo mount nas-ip:/path/to/music /mnt/music

# Make it permanent
echo 'nas-ip:/path/to/music /mnt/music nfs defaults,nofail 0 0' | sudo tee -a /etc/fstab
```

- [ ] Verify your music files are accessible: `ls /mnt/music/`

---

## Phase 2: Email Setup (Gmail)

Using a dedicated Gmail account for StoLat notifications.

- [ ] Log into the Gmail account
- [ ] Enable 2-Step Verification at https://myaccount.google.com/security
- [ ] Create an app password at https://myaccount.google.com/apppasswords
  - Select "Mail" as the app
  - Copy the 16-character password (e.g., `xxxx xxxx xxxx xxxx`)
- [ ] Note the Gmail address and app password

---

## Phase 3: External Service Credentials

### Discogs (optional)

- [ ] Go to https://www.discogs.com/settings/developers
- [ ] Generate a personal access token
- [ ] Note your Discogs username and token

### Volumio (optional)

- [ ] Note your Volumio instance URL (e.g., `http://volumio.local` or its IP)
- [ ] Verify it's reachable from the Pi: `curl http://volumio.local/api/v1/ping`

---

## Phase 4: First-time Deploy

### Initial deploy (sets up everything)

On your development machine:

```bash
# Build and deploy
./deploy.sh <version-tag>
# e.g., ./deploy.sh v0.1.2
```

This builds the JAR, copies all files to the Pi, and starts the containers.

### Configure secrets (first time only)

SSH into the Pi and edit the `.env` file:

```bash
ssh ubuntu@stolat.local
cd ~/stolat
nano .env
```

Fill in all values:

```properties
# Music directory on the Pi
STOLAT_MUSIC_DIR=/mnt/music

# Mail (Gmail with app password)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-stolat-gmail@gmail.com
SPRING_MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
STOLAT_NOTIFICATION_FROM=StoLat <your-stolat-gmail@gmail.com>
STOLAT_NOTIFICATION_RECIPIENT=your-personal-email@example.com

# Discogs (optional)
STOLAT_DISCOGS_USERNAME=your-username
STOLAT_DISCOGS_TOKEN=your-token

# Volumio (optional)
STOLAT_VOLUMIO_URL=http://volumio.local
```

- [ ] `.env` file is configured with all required values

### Start the application

```bash
cd ~/stolat
~/stolat/deploy-pi.sh
```

- [ ] Verify containers are running: `docker compose ps`
- [ ] Check app logs: `docker compose logs -f app`
- [ ] Wait for "Started StoLatApplication" in the logs

---

## Phase 6: Verification

### App access

- [ ] Open `http://stolat.local:8080` in a browser on your local network
- [ ] Login page appears
- [ ] Log in with `user` / `stolat`
- [ ] Birthday view loads (default route)

### Collection scan

- [ ] Navigate to Collection view
- [ ] Click "Scan Collection"
- [ ] Albums appear progressively
- [ ] Release dates fill in over time (background MusicBrainz lookups)

### Discogs scan (if configured)

- [ ] "Scan Discogs" button is visible
- [ ] Click it — vinyl albums appear
- [ ] Albums in both collections show both format icons

### Email notification

- [ ] Check the app logs for "Sending birthday notification on startup"
- [ ] Check your email inbox for the birthday digest
- [ ] If no birthdays today, wait for a day when there are some, or verify
  with: `docker compose logs app | grep -i "notification\|email\|birthday"`

### Volumio (if configured)

- [ ] Click a play button next to a digital album in the Birthday view
- [ ] Album starts playing on Volumio

---

## Phase 7: Ongoing Operations

### View logs

```bash
cd ~/stolat
docker compose logs -f app        # app logs
docker compose logs -f postgres    # database logs
```

### Restart

```bash
docker compose restart app
```

### Update the application

On your development machine:

```bash
./deploy.sh <version-tag>
```

This builds, copies, and restarts everything. The database is preserved.

### Backup database

```bash
docker compose exec postgres pg_dump -U stolat stolat > backup-$(date +%Y%m%d).sql
```

### Restore database

```bash
cat backup-20260321.sql | docker compose exec -T postgres psql -U stolat stolat
```

### Full reset (rescan everything)

```bash
cd ~/stolat
docker compose down -v    # removes database volume
docker compose up -d      # fresh start, will rescan on first use
```

---

## Quick Reference

| What | Command |
|---|---|
| Start | `docker compose up -d` |
| Stop | `docker compose down` |
| Logs | `docker compose logs -f app` |
| Restart | `docker compose restart app` |
| Rebuild | `~/stolat/deploy-pi.sh` |
| Deploy from Mac | `./deploy.sh <version-tag>` |
| DB backup | `docker compose exec postgres pg_dump -U stolat stolat > backup.sql` |
| Full reset | `docker compose down -v && docker compose up -d` |
| App URL | `http://stolat.local:8080` |
| Login | `user` / `stolat` |
