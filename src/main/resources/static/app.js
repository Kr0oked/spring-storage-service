const app = new Vue({
  el: '#app',
  data: {
    page: {
      totalPages: 0,
      totalElements: 0,
      number: 0,
      size: 0,
      numberOfElements: 0,
      content: []
    },
    size: 10,
    sort: ''
  },
  methods: {
    getNextPage: function() {
      this.getPage(this.page.number + 1);
    },
    getPreviousPage: function() {
      this.getPage(this.page.number - 1);
    },
    reloadPage: function() {
      this.getPage(this.page.number);
    },
    getPage: function(page) {
      fetch(`/storage?page=${page}&size=${this.size}&sort=${this.sort}`, {
        method: 'GET',
        headers: {
          Accept: 'application/json'
        }
      })
      .then(response => response.json())
      .then(data => {
        this.page = data;
      });
    },
    upload: function() {
      const data = new FormData();
      data.append('file', document.querySelector('#inputFile').files[0]);

      fetch('/storage/upload', {
        method: 'POST',
        body: data
      })
      .then(response => {
        this.reloadPage();
        $('#uploadModal').modal('hide');
      });
    },
    deleteItem: function(id) {
      fetch(`/storage/${id}`, {
        method: 'DELETE'
      })
      .then(response => this.reloadPage());
    },
    sortBy: function(attribute) {
      if (this.sort.startsWith(attribute) && this.sort.endsWith('asc')) {
        this.sort = `${attribute},desc`;
      } else {
        this.sort = `${attribute},asc`;
      }

      this.reloadPage();
    },
    formatBytes: function(bytes) {
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
      if (bytes === 0) return '';
      const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)), 10);
      if (i === 0) return `${bytes} ${sizes[i]}`;
      return `${(bytes / (1024 ** i)).toFixed(1)} ${sizes[i]}`;
    },
    formatDateString: function(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toUTCString();
    }
  },
  beforeMount() {
    this.reloadPage();
  }
});
