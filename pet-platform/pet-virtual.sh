#!/bin/bash

# Pet Virtual Platform Management Script
# Usage: ./pet-virtual.sh {start|stop|restart|status|log|tail}

APP_NAME=pet-virtual
JAR_NAME=pet-virtual.jar
JAR_PATH=pet-virtual/target/$JAR_NAME
PID_FILE=pet-virtual.pid
LOG_FILE=logs/virtual.log
PROFILE=${SPRING_PROFILES_ACTIVE:-prod}

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
get_pid() {
    if [ -f "$PID_FILE" ]; then
        cat "$PID_FILE"
    fi
}

is_running() {
    local pid=$(get_pid)
    if [ -z "$pid" ]; then
        return 1
    fi
    if ps -p "$pid" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

start() {
    if is_running; then
        echo -e "${YELLOW}$APP_NAME is already running (PID: $(get_pid))${NC}"
        return 1
    fi

    if [ ! -f "$JAR_PATH" ]; then
        echo -e "${RED}Error: JAR file not found at $JAR_PATH${NC}"
        echo "Please run 'mvn clean package' first"
        return 1
    fi

    echo -e "${GREEN}Starting $APP_NAME...${NC}"
    mkdir -p logs

    nohup java -jar "$JAR_PATH" \
        --spring.profiles.active=$PROFILE \
        > "$LOG_FILE" 2>&1 &

    local pid=$!
    echo $pid > "$PID_FILE"

    # Wait for startup
    sleep 3

    if is_running; then
        echo -e "${GREEN}$APP_NAME started successfully (PID: $pid)${NC}"
        echo -e "${GREEN}View logs: ./pet-virtual.sh log${NC}"
        return 0
    else
        echo -e "${RED}Failed to start $APP_NAME${NC}"
        rm -f "$PID_FILE"
        return 1
    fi
}

stop() {
    if ! is_running; then
        echo -e "${YELLOW}$APP_NAME is not running${NC}"
        rm -f "$PID_FILE"
        return 0
    fi

    local pid=$(get_pid)
    echo -e "${YELLOW}Stopping $APP_NAME (PID: $pid)...${NC}"

    kill "$pid"

    # Wait for process to stop (max 30 seconds)
    local count=0
    while is_running && [ $count -lt 30 ]; do
        sleep 1
        count=$((count + 1))
        echo -n "."
    done
    echo

    if is_running; then
        echo -e "${RED}Failed to stop gracefully, forcing shutdown...${NC}"
        kill -9 "$pid"
        sleep 1
    fi

    rm -f "$PID_FILE"
    echo -e "${GREEN}$APP_NAME stopped${NC}"
}

status() {
    if is_running; then
        local pid=$(get_pid)
        echo -e "${GREEN}$APP_NAME is running (PID: $pid)${NC}"

        # Show memory usage
        if command -v ps &> /dev/null; then
            local mem=$(ps -p "$pid" -o rss= 2>/dev/null)
            if [ -n "$mem" ]; then
                local mem_mb=$((mem / 1024))
                echo "Memory usage: ${mem_mb}MB"
            fi
        fi
        return 0
    else
        echo -e "${RED}$APP_NAME is not running${NC}"
        return 1
    fi
}

show_log() {
    if [ -f "$LOG_FILE" ]; then
        cat "$LOG_FILE"
    else
        echo -e "${RED}Log file not found: $LOG_FILE${NC}"
    fi
}

tail_log() {
    if [ -f "$LOG_FILE" ]; then
        echo -e "${GREEN}Tailing log file (Ctrl+C to exit)...${NC}"
        tail -f "$LOG_FILE"
    else
        echo -e "${RED}Log file not found: $LOG_FILE${NC}"
    fi
}

# Main
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        sleep 2
        start
        ;;
    status)
        status
        ;;
    log)
        show_log
        ;;
    tail)
        tail_log
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|log|tail}"
        echo ""
        echo "Commands:"
        echo "  start   - Start the application"
        echo "  stop    - Stop the application"
        echo "  restart - Restart the application"
        echo "  status  - Check application status"
        echo "  log     - Show full log"
        echo "  tail    - Tail log in real-time"
        exit 1
        ;;
esac

exit $?
