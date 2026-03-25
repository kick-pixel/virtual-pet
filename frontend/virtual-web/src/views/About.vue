<script setup lang="ts">
import { ref } from 'vue'
import { Video, Users, Share2, Calendar, Archive, Twitter, Mail } from 'lucide-vue-next'

const teamMembers = [
  {
    name: 'Rita',
    role: 'CEO',
    avatar: 'https://i.ibb.co/nNFHvv6W/rita-ai.png',
    desc: [
      'CBO of U.S Listed Company',
      'Manager of LVMH'
    ]
  },
  {
    name: 'Mark',
    role: 'CTO',
    avatar: 'https://i.ibb.co/NdK4Q5rH/mark-ai.png',
    desc: [
      '8 years Crypto experience',
      'Engineering Degree of HKUST',
      'Co-founder of ETHHK'
    ]
  }
]

const partners = [
  {
    role: 'LEAD PARTNER',
    name: 'Animoca Brands',
    desc: 'Leveraging deep industry networks.'
  },
  {
    role: 'LEAD PARTNER',
    name: 'Web3 Labs',
    desc: 'Strategic ecosystem support.'
  },
  {
    role: 'INVESTMENT / VC',
    name: 'Arkstream Capital',
    desc: 'Strong financial backing.'
  },
  {
    role: 'INVESTMENT / VC',
    name: 'ICC Ventures',
    desc: 'Strategic growth capital.'
  }
]
</script>

<template>
  <div class="about-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <h1 class="hero-title">OUR INNOVATIVE<br>PLATFORM</h1>
      <p class="hero-subtitle">
        A blockchain-based AI-driven pet nurturing platform that builds a complete Web3 revenue ecosystem.
      </p>
    </div>

    <!-- Core Product Section -->
    <div class="section-container">
      <h2 class="section-title">Core Product: The AI Pet Ecosystem</h2>
      <p class="section-subtitle">Blockchain-based, AI-nurturing platform</p>

      <div class="product-grid">
        <!-- Top Row (3 cards) -->
        <div class="product-card">
          <div class="icon-box">
            <Video class="w-8 h-8 text-amber-700" />
          </div>
          <h3>AI Video Generation</h3>
          <span class="highlight">The Engine.</span>
          <p>Create professional-grade short-form video content instantly.</p>
        </div>

        <div class="product-card">
          <div class="icon-box">
            <Users class="w-8 h-8 text-amber-700" />
          </div>
          <h3>Social Life Circle</h3>
          <span class="highlight">The Retention.</span>
          <p>Pet Profiles, Achievement Badges, and "Follow-to-Earn" mechanics.</p>
        </div>

        <div class="product-card">
          <div class="icon-box">
            <Share2 class="w-8 h-8 text-amber-700" />
          </div>
          <h3>KOL Ecosystem</h3>
          <span class="highlight">The Growth.</span>
          <p>Turning unique AI pets into digital influencers ready for brand collaborations.</p>
        </div>

        <!-- Bottom Row (2 cards) -->
        <div class="product-card centered-card">
          <div class="icon-box">
            <Calendar class="w-8 h-8 text-amber-700" />
          </div>
          <h3>Daily Reminders</h3>
          <span class="highlight">Keeping You Connected.</span>
          <p>Personalized task management for your AI pet and your own life circle.</p>
        </div>

        <div class="product-card centered-card">
          <div class="icon-box">
            <Archive class="w-8 h-8 text-amber-700" />
          </div>
          <h3>Memorial Functions</h3>
          <span class="highlight">The Emotional Moat.</span>
          <p>Immortalize memories on the blockchain forever.</p>
        </div>
      </div>
    </div>

    <!-- Team & Strategic Ecosystem Section -->
    <div class="split-section">
      <!-- Team -->
      <div class="team-container">
        <div class="section-header">
          <div class="icon-circle">
            <Users class="w-5 h-5 text-amber-600" />
          </div>
          <h2>The Team</h2>
        </div>
        
        <div class="team-grid">
          <div v-for="member in teamMembers" :key="member.name" class="team-card">
            <div class="avatar-wrapper">
              <img :src="member.avatar" :alt="member.name" />
            </div>
            <h3>{{ member.name }}</h3>
            <span class="role">{{ member.role }}</span>
            <ul class="desc-list">
              <li v-for="(item, idx) in member.desc" :key="idx">{{ item }}</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Strategic Ecosystem -->
      <div class="strategic-container">
        <div class="section-header">
          <div class="icon-circle"></div>
          <h2>Strategic Ecosystem</h2>
        </div>
        <span class="sub-header">Backing & Partners</span>

        <div class="partners-grid">
          <div v-for="partner in partners" :key="partner.name" class="partner-card">
            <span class="partner-role">{{ partner.role }}</span>
            <h3>{{ partner.name }}</h3>
            <p>{{ partner.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer CTA -->
    <div class="footer-cta">
      <div class="cta-badge">
        <Mail class="w-4 h-4" />
        <span>Join us now</span>
      </div>
      <h2>Seeking launch capital as we are ready to push My AI PET to the next frontier.</h2>
      
      <a href="https://x.com/MYAIPETS" target="_blank" class="twitter-btn">
        <Twitter class="w-5 h-5 text-[#1DA1F2]" />
        <span>@MYAIPETS</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.about-container {
  min-height: 100vh;
  background: #FFE8D1; /* Light beige/peach background from image */
  color: #4A3B32;
  font-family: 'Inter', sans-serif;
  padding: 60px 20px 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Hero Section */
.hero-section {
  text-align: center;
  margin-bottom: 60px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.1;
  color: #3D2C25;
  margin-bottom: 20px;
  text-transform: uppercase;
}

.hero-subtitle {
  font-size: 16px;
  color: #8C7B70;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.5;
}

/* Section Common */
.section-container {
  width: 100%;
  max-width: 1200px;
  margin-bottom: 80px;
  text-align: center;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #3D2C25;
  margin-bottom: 8px;
}

.section-subtitle {
  font-size: 14px;
  color: #8C7B70;
  margin-bottom: 40px;
}

/* Product Grid */
.product-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 24px;
}

.product-card {
  background: linear-gradient(180deg, #FFF9F2 0%, #FFF0E3 100%);
  border-radius: 20px;
  padding: 32px 24px;
  width: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 4px 20px rgba(234, 199, 175, 0.4);
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.icon-box {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #FFE4C4 0%, #FFD6A5 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.8), 0 4px 8px rgba(210, 180, 160, 0.2);
}

.product-card h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #3D2C25;
}

.highlight {
  font-size: 12px;
  font-weight: 700;
  color: #E68D4F;
  margin-bottom: 12px;
  display: block;
}

.product-card p {
  font-size: 13px;
  color: #8C7B70;
  line-height: 1.4;
}

/* Split Section (Team & Ecosystem) */
.split-section {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  gap: 40px;
  margin-bottom: 80px;
}

.team-container, .strategic-container {
  flex: 1;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #3D2C25;
}

.icon-circle {
  width: 32px;
  height: 32px;
  background: #FFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sub-header {
  display: block;
  font-size: 14px;
  color: #8C7B70;
  margin-bottom: 24px;
  margin-left: 44px;
}

/* Team Grid */
.team-grid {
  display: flex;
  gap: 20px;
}

.team-card {
  flex: 1;
  background: linear-gradient(180deg, #FFF9F2 0%, #FFF0E3 100%);
  border-radius: 20px;
  padding: 32px 20px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(234, 199, 175, 0.4);
}

.avatar-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: #FFF;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #FFF;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

.avatar-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: #3D2C25;
  margin-bottom: 4px;
}

.role {
  font-size: 12px;
  font-weight: 700;
  color: #E68D4F;
  margin-bottom: 16px;
  display: block;
}

.desc-list {
  list-style: none;
  padding: 0;
  margin: 0;
  text-align: left;
  font-size: 12px;
  color: #8C7B70;
}

.desc-list li {
  margin-bottom: 6px;
  position: relative;
  padding-left: 12px;
}

.desc-list li::before {
  content: "•";
  color: #E68D4F;
  position: absolute;
  left: 0;
  font-weight: bold;
}

/* Partners Grid */
.partners-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.partner-card {
  background: linear-gradient(180deg, #FFF9F2 0%, #FFF0E3 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(234, 199, 175, 0.3);
}

.partner-role {
  font-size: 10px;
  font-weight: 700;
  color: #E68D4F;
  text-transform: uppercase;
  margin-bottom: 8px;
  display: block;
}

.partner-card h3 {
  font-size: 16px;
  font-weight: 700;
  color: #3D2C25;
  margin-bottom: 6px;
}

.partner-card p {
  font-size: 11px;
  color: #8C7B70;
  line-height: 1.3;
}

/* Footer CTA */
.footer-cta {
  width: 100%;
  max-width: 800px;
  background: linear-gradient(180deg, #FFF9F2 0%, #FFE4C4 100%);
  border-radius: 24px;
  padding: 48px 40px;
  text-align: center;
  position: relative;
  box-shadow: 0 8px 30px rgba(234, 199, 175, 0.5);
  border: 2px solid #FFF;
}

.cta-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #FFF;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #3D2C25;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.footer-cta h2 {
  font-size: 28px;
  font-weight: 700;
  color: #3D2C25;
  margin-bottom: 32px;
  line-height: 1.3;
}

.twitter-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: #FFF;
  padding: 12px 24px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  color: #3D2C25;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transition: transform 0.2s;
}

.twitter-btn:hover {
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 900px) {
  .split-section {
    flex-direction: column;
  }
  
  .product-grid {
    flex-direction: column;
    align-items: center;
  }
  
  .hero-title {
    font-size: 32px;
  }
}
</style>