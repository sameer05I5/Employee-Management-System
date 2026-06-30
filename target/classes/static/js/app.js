document.addEventListener("DOMContentLoaded", () => {
    // 1. Toast Notification system
    window.showToast = (message, type = 'success') => {
        const container = document.getElementById("toast-container");
        if (!container) return;

        const toast = document.createElement("div");
        toast.className = `toast-msg flex items-center w-full max-w-xs p-4 text-gray-200 rounded-lg shadow glass-card mb-3`;
        
        let iconColor = 'text-green-400';
        let iconSvg = `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`;
        
        if (type === 'error') {
            iconColor = 'text-red-400';
            iconSvg = `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`;
        } else if (type === 'warning') {
            iconColor = 'text-yellow-400';
            iconSvg = `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>`;
        }

        toast.innerHTML = `
            <div class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 rounded-lg bg-slate-900 ${iconColor}">
                ${iconSvg}
            </div>
            <div class="ml-3 text-sm font-medium">${message}</div>
            <button type="button" class="ml-auto -mx-1.5 -my-1.5 rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-slate-700 inline-flex h-8 w-8 text-gray-400 hover:text-white" data-dismiss-target="#toast-success" aria-label="Close">
                <span class="sr-only">Close</span>
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
        `;

        container.appendChild(toast);

        // Bind dismiss button
        toast.querySelector("button").addEventListener("click", () => {
            toast.remove();
        });

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (toast.parentNode) {
                toast.classList.add("opacity-0", "transition-opacity", "duration-500");
                setTimeout(() => toast.remove(), 500);
            }
        }, 5000);
    };

    // 2. Load page animations
    const cards = document.querySelectorAll(".glass-card");
    cards.forEach((card, index) => {
        card.style.opacity = "0";
        card.style.transform = "translateY(15px)";
        card.style.transition = "opacity 0.4s ease-out, transform 0.4s ease-out";
        setTimeout(() => {
            card.style.opacity = "1";
            card.style.transform = "translateY(0)";
        }, index * 80);
    });

    // 3. User profile photo dynamic upload preview (if profile photupload element is active)
    const fileInput = document.getElementById("profile-upload");
    if (fileInput) {
        fileInput.addEventListener("change", (e) => {
            const preview = document.getElementById("profile-preview");
            if (preview && e.target.files && e.target.files[0]) {
                const reader = new FileReader();
                reader.onload = (event) => {
                    preview.src = event.target.result;
                };
                reader.readAsDataURL(e.target.files[0]);
            }
        });
    }
});
